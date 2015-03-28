# Introduction #

Welcome to JavaDrone!  We hope to attract more contributors who can make this project even better!  This introduction is meant to get beginners started on getting JavaDrone operable.

# Details #

#1:  Download Eclipse (Java Developers or Java EE Developers)
http://www.eclipse.org/downloads/

#2:  Open Eclipse.  Using the Help menu, there is an "Eclipse Marketplace".  Open the Eclipse Marketplace.  Search for "Maven".  You should find "Maven Integration for Eclipse".  Install.  This might take a while.  If you are not familiar with Maven, it is WORTH IT!  It will setup your project and download all of the necessary libraries (with correct versioning) for your build path.  Feel free to read more about it and keep in mind that the integration with Eclipse is really powerful and makes your life so much easier.  (... Build Path setup for Idiots like me :)... one of the best coding creations)

#3:  Download entire JavaDrone source.  Later this or another wiki can be created about "Cloning" the project or even becoming a contributing member.  However, first we just want to get you started and excited that you CAN modify the code and see it work on your drone.  To download the whole source, you can go to the Source link at the top of the Page (Project Home, Downloads, Wiki, Issues, Source).  Once on the Source page, go to "Browse".  You should be at the very top of the directory "hg".  If not, browse to the top directory. You should see the "Download Zip" link.  Use this to download a zip.

#4:  Extract zip file

#5:  In Eclipse, do a Maven Import.  Go "File", "Import", and under Maven click on "Existing Maven Projects".  In the root directory, browse to the unzipped folder.  It should start analyzing and it will take a while.  Just sit back and know that Maven is doing a lot of work for you.  After some time, it should come up with the 4 projects (controltower, controltower-android, javadrone-api, javadrone-utils).  I would import all of them by clicking Ok.  This will take a while too.  If you do not have the Android SDK installed, it will also prompt you to do that.  Follow the prompts and install it.  After it is complete, you should have 4 projects added to your working area.  Hurray!  It just did a lot of work for you.  Love Maven.  If you have any issues with the Android project, go to the project properties, the "Android" menu and click the checkbox to use Android in the project.  Click Ok.  Rick click on project and go to Maven, Update Project.

#6:  Connect to your ardrone network on your computer.

#7:  Run the ControlTower project as a Java Application.  You can also Debug, etc. as normal.

#9:  Change things and HAVE FUN!

More to come ...

Need to go into how to add a PS3 controller to your computer to use as a controller.

Need to give information on the why and how of cloning the project.

Need to give information on the why and how to become a contributing member.

Let me know if you have any quesitons or issues and I will try to add to this Wiki or another if I know the answer.

Thanks!!!