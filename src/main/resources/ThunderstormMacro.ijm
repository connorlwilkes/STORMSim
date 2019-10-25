run("Camera setup", "offset=300.0 isemgain=false photons2adu=1.0 pixelsize=80.0");

for(i = 100; i <= 300; i += 10) {
 	open("C:/Users/virtualreality/IdeaProjects/STORMSim/python/resources/experiments/PSF_variance/Image-PSF_VARIANCE-" + i + ".0-2108/Image-PSF_VARIANCE-" + i + ".0.tif");
 	run("Run analysis", "filter=[Wavelet filter (B-Spline)] scale=2.0 order=3 detector=[Centroid of connected components] watershed=false threshold=std(Wave.F1) estimator=[PSF: Gaussian] sigma=1.6 fitradius=3 method=[Weighted Least squares] full_image_fitting=false mfaenabled=false renderer=[Averaged shifted histograms] magnification=5.0 colorizez=false threed=false shifts=2 repaint=50");
 	run("Show results table", "action=merge zcoordweight=0.1 offframes=1 dist=20.0 framespermolecule=0");
 	run("Export results", "filepath=C:/Users/virtualreality/IdeaProjects/STORMSim/python/resources/experiments/PSF_variance/Image-PSF_VARIANCE-" + i + ".0-2108/TablePSF_Variance-" + i + "-ThunderSTORM.csv fileformat=[CSV (comma separated)] sigma=false intensity=false chi2=false offset=false saveprotocol=true x=true y=true bkgstd=false id=true uncertainty=false STORMFrame=false detections=false");
 	run("Close");
 	run("Close All");
 }