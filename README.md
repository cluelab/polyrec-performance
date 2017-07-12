# polyrec-performance

Application that compute the performance (error rate) on a given dataset of the [PolyRec unistroke gesture recognizer](https://github.com/cluelab/polyrec).

Usage:
polyrec-perfomance datasetDirectory numTests=10 rotationInvariant=false fullName=false maxTempl=9 trainingN=-1

Example:
- download $1 gesture data set from http://depts.washington.edu/madlab/proj/dollar/xml.zip and extract it
- remove the directory "s01 (pilot)"
- run the application giving as first parameter the path of the $1 dataset