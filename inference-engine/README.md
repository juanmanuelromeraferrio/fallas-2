#Inference Engine

## Running Locally

Make sure you have [Maven](https://maven.apache.org/) and [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed.

## Running with Eclipse

```sh
$ git clone https://github.com/juanmanuelromeraferrio/fallas-2.git # or clone your own fork
$ cd fallas-2/inference-engine
$ mvn eclipse:eclipse
```

1. From the main menu bar, select  command link File > Import.... The Import wizard opens.
2. Select General > Existing Project into Workspace and click Next.
3. Choose either Select root directory or Select archive file and click the associated Browse to locate the directory or file containing the projects.
4. Under Projects select the project or projects which you would like to import.
5. Click Finish to start the import.
6. Select Run > Run Configurations > Select InferenceEngineMain as Main class
7. Run

## Running in console

En root del proyecto: 

```sh
$ mvn assembly:assembly
$ java -jar target/InferenceEngineExecutable.jar
```

