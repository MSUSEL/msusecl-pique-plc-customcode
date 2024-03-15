# msusecl-pique-plc-customcode

## Introduction
This project is an operationalized PIQUE model for the assessment of quality in PLC customcode.

Because of the various development environment challenges when dealing with numerous 3rd party applications,
this project is also provided as a packaged standalone docker image. That image is available
[here](https://hub.docker.com/repository/docker/msusel/pique-plc-customcode/general).

## Tools and 3rd party libraries
These tools and 3rd party libraries will be automatically pulled with the docker image
* [PIQUE-core](https://github.com/MSUSEL/msusel-pique) version 0.9.5_2 (packaged as a layer in the image)
* PIQUE-plc-customcode relies upon output from the tool CODESYS, specifically a txt file of rule violations and a csv file of metrics. 
For examples of these files, please refer to this [repository](https://github.com/MSUSEL/benchmarks/tree/main/plc-customcode/codesys-output) 

The dockerfile has been designed to easily adjust version information as new versions are released.

## Run environment
#### Docker
docker engine 20.10.24 (not tested with versions 21+)

The image for this project is hosted on dockerhub
[here](https://hub.docker.com/repository/docker/msusel/pique-plc-customcode/general). Instructions to download
and run are supplied [below](https://github.com/MSUSEL/msusecl-pique-plc-customcode/tree/master#running)


#### not Docker
It is not suggested to run PIQUE-plc-customcode without the pre-built docker image, but all files and configs
are supplied on this repository.


## Running
1. Download and install [Docker engine](https://docs.docker.com/engine/install/)
2. With Docker engine installed, pull the latest version of this project: `docker pull msusel/pique-plc-customcode:latest`
3. Navigate to a working directory for this project
4. Create two directories, "input" and "output". 
5. Move CODESYS analysis output files (txt file and csv file) into their own directory, named appropriately. Examples are available [here](https://github.com/MSUSEL/benchmarks/tree/main/plc-customcode/codesys-output)
   * Move these files into the "input directory"
6. The resulting directory structure should look like this:
```
├── $WORKDIR
│   ├── input
│   │   ├── project_to_analyze
│   │   │   ├── CODESYS_rules_output.txt
│   │   │   ├── CODESYS_metrics_output.csv
│   ├── output
```
11. Run the command `docker run -it --rm -v /path/to/working/directory/input:/input -v /path/to/working/directory/output:/output msusel/pique-plc-customcode:latest`
12. Results will be generated in the 'output' directory

Funding Agency:

[<img src="https://www.dhs.gov/sites/default/files/2023-03/ST_RGB_Hor_Blue_at20.svg" width="20%" height="20%">](https://www.dhs.gov/science-and-technology)