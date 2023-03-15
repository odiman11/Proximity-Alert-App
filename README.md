# ProximityAlerts

Android radar to know who is around you.

<img src="https://user-images.githubusercontent.com/111682395/225279966-9b7691b2-5fc5-4516-a0ca-9c5cc024f226.jpg" width="200" />

## Table of Contents

* [About the Project](#about-the-project)
  * [Built With](#built-with)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
* [Usage](#usage)


## About the Project

*<sub>this is a collaborative project, this is only the front-end side, back-end is part of a different project by a colleague.</sub>
 
Front-End tool to save and track targets. 
It organises the targets in their proper X and Y coordinates on the radar screen along with a functional compass.

<img src="https://user-images.githubusercontent.com/111682395/225285616-e01c45e1-3057-4144-a6d0-31c1e90d1c2f.jpg" width="300" />

Radar 'scan' radius can be easily changed
and a click on any target will present its data on an information screen
with extra features for saving targets, marking target in a different color and adding description for each target

<img src="https://user-images.githubusercontent.com/111682395/225299713-9010d942-e2e2-4524-b28a-f1c0fa9a2779.jpg" width="300" />

### Built With

* Java
* Android Studio

## Getting Started

While installing the app is possible, it wont work without a proper server side to communicate targets in the proper protocol

### Prerequisites

* Android Studio
* Android phone (api 24 and above) - Optional

### Installation
1. clone the repository into android studio.
2. go into /app/src/main/java/com/portfolio/proximityalerts/UdpClient.java and change HOST and PORT to proper server details and NAME and MMSI into anything you like (if a connection will be unavailable no targets will be shown but app other features will work)
3. connect phone with developer options enabled via USB and run the app on your phone through android studio or use the built-in emulator.

## Usage

To enable or disable feature you can go into the setting option in the top-right menu

![PAmenu](https://user-images.githubusercontent.com/111682395/225308312-53fee1ce-a17a-47f7-a60c-da2776907d83.jpg)

<img src="https://user-images.githubusercontent.com/111682395/225308348-91fc1c30-19e8-4af9-afb5-1f48e620d12d.jpg" width="200" />

where you will also find a link to the 'favorites' screen where you can see and edit all saved targets

<img src="https://user-images.githubusercontent.com/111682395/225308370-ef612f54-971a-4e4c-87ee-065fa33b0e94.jpg" width="200" />


