# Project build insruction #

1. Download and install Maven 3 or greater

2. Download and install Android SDK http://developer.android.com/sdk/index.html

3. Set system variable ANDROID\_HOME  (should point to Android SDK installation directory)

4. Execute

```
mvn install
```

Builded files description:

javadrone-api/target/javadrone-api.jar - Javadrone API

controltower/target/controltower.jar - ControlTower Demo application


controltower/target/controltower-full.jar  - ControlTower Demo application (with all dependency libraries packaged inside. Native libraries also included)

To start  ControlTower execute

```
java -jar controltower/target/controltower-full.jar
```

controltower-android/target/controltower-android.apk - Android Sample application

# Developer build insructions #

1. Download and install Maven 3 or greater

2. Download and install Android SDK http://developer.android.com/sdk/index.html

3. Set system variable ANDROID\_HOME  (should point to Android SDK installation directory)


4. Execute

```
mvn install
```

5. Execute

```
mvn eclipse:eclipse
```

this command will generate Eclipse project files

6. Download and install Eclipse  http://www.eclipse.org/downloads/

7. Instal Eclipse ADT (Android Developer Tools)  plugin http://developer.android.com/tools/sdk/eclipse-adt.html

8. Import projects into eclipse  File -> Import -> General -> Existing project into Eclipse

9. Create M2\_REPO variable  Window -> Preference -> Java -> Class Path varaibles -> NEW

> Inside variable specify path to local maven repository ( on MacOS and Linux usually path is ~/.m2/repository on windows the same user\_home\_path/.m2/repository)

![http://www.mkyong.com/wp-content/uploads/2010/09/add-m2_repo-to-eclipse-ide.png](http://www.mkyong.com/wp-content/uploads/2010/09/add-m2_repo-to-eclipse-ide.png)

10. Start ControlTover by executing _com.codeminders.controltower.ControlTower_ class

# maven commands list #

1. Build project
```
mvn install
```
2. Clean project
```
mvn clean
```
3. Generate Eclipse project files
```
mvn eclipse:eclipse
```
4. Clean Eclipse project files
```
mvn eclipse:clean
```