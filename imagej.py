from __future__ import with_statement, division
from random import random
from ij import IJ, WindowManager
from ij.gui import GenericDialog, DialogListener, WaitForUserDialog
from ij.util import Tools
from ij.io import DirectoryChooser
from loci.plugins import BF
from loci.plugins.in import ImporterOptions
from ij.plugin import ImageCalculator, Thresholder
from ij.process import ImageProcessor

RED_CHANNEL = 0
GREEN_CHANNEL = 1
BLUE_CHANNEL = 2

def image_selector_dialog():
	image_selector = GenericDialog("Select image to use")
	image_selector.addChoice("Image:", ["Red Channel", "Green Channel"], "Red Channel")
	image_selector.showDialog()
	if image_selector.wasCanceled():
		return None
	if image_selector.getNextChoice() == "Red Channel":
		return RED_CHANNEL
	return GREEN_CHANNEL

minThreshold = 0
maxThreshold = 0
def update_image_threshold(image, min_threshold, max_threshold):
	IJ.run("Options...", "iterations=10000 count=1 black")
	Thresholder.setMethod("Default")
	Thresholder.setBackground("Dark")
	IJ.setThreshold(image, min_threshold, max_threshold, "Black & White")

class MyListener(DialogListener): 
	def __init__(self, dialog, image, minThreshold, maxThreshold):
		super(MyListener, self).__init__()
		self.minThreshold = minThreshold
		self.maxThreshold = maxThreshold
		self.dialog = dialog
		self.image = image
		
	def dialogItemChanged(self, gd, event):
		if event == None:
			return
		okButton = self.dialog.getButtons()[0]
		okButton.setEnabled(False)
		source = event.getSource()
		sliders = gd.getSliders()
		number_fields = gd.getNumericFields()
		for i in range(0, sliders.size()):
			if source == number_fields.elementAt(i):
				if i == 0:
					newMin = Tools.parseDouble(source.getText())
					if newMin > self.maxThreshold:
						self.minThreshold = self.maxThreshold
						return False
					else:
						self.minThreshold = newMin
				else:
					newMax = Tools.parseDouble(source.getText())
					if self.minThreshold > newMax:
						self.maxThreshold = self.minThreshold
						return False
					self.maxThreshold = newMax
				update_image_threshold(self.image, self.minThreshold, self.maxThreshold)
		return True


def stop_dialog(image):
	minThreshold = 50
	maxThreshold = 255
	IJ.run(image, "Out [-]", "");
	IJ.run(image, "Out [-]", "");
	newImage = image.duplicate()
	IJ.run(newImage, "8-bit", "");
	newImage.show("With Threshold applied")
	newImage.setDisplayMode(IJ.GRAYSCALE)
	
	IJ.run(newImage, "Out [-]", "");
	IJ.run(newImage, "Out [-]", "");
	newImage.getWindow().setLocation(900,60)
	update_image_threshold(newImage, 50,255)
	newImage.repaintWindow()
	gd = GenericDialog("Threshold")
	gd.addSlider("Minimum histogram value", 0, 255, minThreshold)
	gd.addSlider("Maximum histogram value", 0, 255, 255)
	gd.addDialogListener(MyListener(gd, newImage, minThreshold, maxThreshold))
	gd.showDialog()
	if gd.wasCanceled():
		image.close()
		newImage.changes = False
		newImage.close()
		return None
	else:
#		IJ.run("Fill Holes")
		image.close()
		return newImage

def open_images(directory_path, filename, image_to_calculate):
	from os.path import join
	options = ImporterOptions()
	options.setSplitChannels(True)
	options.id = join(directory_path, filename)
	img = BF.openImagePlus(options)
	cell_image = img[image_to_calculate]
	
	nuclei_image = None
	if len(img) == 2:
		nuclei_image = img[1]
	else:
		nuclei_image = img[BLUE_CHANNEL]
	return (cell_image, nuclei_image)

# It's best practice to create a function that contains the code that is executed when running the script.
# This enables us to stop the script by just calling return.
def run_script():
	dir = DirectoryChooser("Select a directory where the ZVI files are stored")
	directoryPath = dir.directory
	from os import listdir
	from os.path import isfile, join
	onlyZviFiles = [f for f in listdir(directoryPath) if isfile(join(directoryPath, f)) and f.endswith(".zvi")]
	image_to_calculate = image_selector_dialog()
	if image_to_calculate is None:
		print("Canceled the selection")
		return

	for zviFile in onlyZviFiles:
		print("going to open {}".format(zviFile))
		(cell_image, nuclei_image) = open_images(directoryPath, zviFile, image_to_calculate)
		
		cell_image.show("original image")
		output_cell_image = stop_dialog(cell_image)
		if output_cell_image is None:
			continue
		
		nuclei_image.show("original image")
		output_nuclei_image = stop_dialog(nuclei_image)
		if	output_nuclei_image is None:
			cell_image.changes = False
			cell_image.close()
			output_cell_image.change = False
			output_cell_image.close()
			continue

		ic = ImageCalculator()
		imp1 = WindowManager.getImage(output_cell_image.getTitle())
		imp2 = WindowManager.getImage(output_nuclei_image.getTitle())
		imp3 = ic.run("Subtract create", imp1, imp2)

		imp3.show()
		IJ.run(output_cell_image, "Set Measurements...", "area limit add redirect=None decimal=3")
		IJ.run(output_cell_image, "Analyze Particles...", "size=0.50-Infinity show=[Masks] display exclude clear summarize")
		WaitForUserDialog("yeah is it good?").show()
		WindowManager.getCurrentImage().close()
		imp3.changes = False
		imp3.close()
		imp1.changes = False
		imp1.close()
		imp2.changes = False
		imp2.close()
		output_cell_image.changes = False
		output_cell_image.close()
		output_nuclei_image.changes = False
		output_nuclei_image.close()
# If a Jython script is run, the variable __name__ contains the string '__main__'.
# If a script is loaded as module, __name__ has a different value.
if __name__ in ['__builtin__','__main__']:
    run_script()