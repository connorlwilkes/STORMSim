# STORMSim: STORM Microscopy Simulator

## Developing a Tool for the Simulation of Realistic STORM Microscopy Image Data with a view to Develop Machine Learning Techniques to Aid in STORM Image Reconstruction

An ImageJ plugin that produces experimentally accurate simulations of from the STORM microscopy technique to be used to
generate data to train Machine Learning models or to help people understand STORM better.

Microscopy is one of the most important research tools utilised in a vast majority of biological research. However the 
diffraction limit discovered by Abbe in 1873 prevents imaging of specimens below 250nm in the XY dimension and 550nm in 
the Z dimension. A large number of biological entities are smaller than this limit and therefore this poses a problem. 
Traditional solutions to this problem involved fixing the specimen however this means the specimen must be dead. Recent 
advances have created techniques known as super resolution microscopy that enable users to overcome this diffraction 
limit. One such example of this technique is Stochastic Optical Reconstruction Microscopy (STORM). STORM uses the 
stochastic nature of photoswitchable fluorophores to achieve this. A simulator of this technique was built as a tool to 
visualise this technique. 

The tool was used to benchmark traditional STORM reconstruction techniques and simulated data was further used as an 
input into deep neural networks with the aim to build reconstruction techniques.

In addition to the code for the ImageJ plugin this repo also contains:
1. The written thesis that gives a more thorough and robust understanding of the project in the home directory
2. Python scripts that calculate the optimal mapping between two sets and measure the distance between two sets using a number of metrics
3. Keras code for an autoencoder and convolutional neural netwok to reconstruct frames of STORM 

### Usage

#### ImageJ Plugin

1. Import the pom.xml with Maven
2. Run the main method
3. ImageJ should open in separate window & the plugin can be found in the plugins menu

#### Python 

1. Set up a venv and run pip install on the requirements.txt
2. Replace the relevant file paths in the python files

### Examples
 
![Alt text](./examples/example5.gif)
![Alt text](./examples/example6.gif)