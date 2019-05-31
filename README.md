
# ServiceComparison

It's a java program with a javafx interface based that goes with serviceidentification.
The principle is to compare architecture proposals from txt files with a reference architecture and see which one is the closest to the right model

## Build and Run :

You need only maven and a 1.8 jdk to run this project

**Build :**

    mvn clean install
    
**Run :**

    mvn exec:java "-Dexec.args=arg1 arg2 (arg3 arg4)"
    
**Build and Run  :**

    mvn clean install exec:java "-Dexec.args=arg1 arg2 (arg3 arg4)"

**Arguments :**
This program takes two arguments:

    argument 1 : the path to the directory where the txt files for the comparison are stored
    argument 2 : the name of the reference model (path from the directory of argument 1 and wih the extension)
    OPTIONAL (
	    argument 3 : Path to a kdm_xmi file for the turboMQ metric
	    argument 4 Name of the kdm model (should be the name of the project)
    )

> There can only be 5 model files loaded at a time for the comparison

    
**Example :**

    mvn clean install exec:java "-Dexec.args=C:\Users\user\bench reference.txt C:\Users\user\stuff\test_kdm.xmi test"


# Necessary files

## Service Model

In order to compare the services you need to provide service files with a json like format and with a txt extension, these files must follow this syntax :

    [{Class1, Class2}, {Class3, Class4, Class5}]

This would be an architecture with two services, the first composed of two classes and the second of three.

### Example

    [{Memory, CodeWriter}, {Function, testReader, ShortcutTest, Shortcut, parser, Main, BfckContainer, CommandPerform}, {BfWriter, MultiIncrDecr, ExecuteException}, {Bfck, Arguments}]

## KDM Model

In order to use the TurboMQ metric, you need to have a generated a kdm xmi model from the java project you test beforehand.  
To do this you need the [MoDisco](https://www.eclipse.org/MoDisco/) plugin for eclipse and to discover xmi model from your project source code and then discover KDM model from this model, you will then obtain a xmi file containing a kdm model that you can feed to this program. Inside this file there should also be the model name that you need to put as an argument.


# Interface

![Interface](http://image.noelshack.com/fichiers/2018/32/5/1533928143-servicecomp2.png)

***Right canvas***
On the right there is a dendrogram graph representing the service architecture of the currently selected model. You can zoom in and out with the + and - keys

***Left bar***
On top you can see information regarding the current model and the result of each metric comparing it to the reference (except turboMQ comparing him to the provided model)

There is also a button for each architecture slicing analyzed, the first one always being the reference model.

Finally there is a button to save the dendrogram as an SVG picture in the current execution folder

# Service clusters comparison

## Greedy Jaccard Similarity

To compare the different architecture I use a custom made greedy algorithm.
It is based on the [Jaccard Similarity](https://en.wikipedia.org/wiki/Jaccard_index) to compute a similarity between two services.

However as we deal with sets of services there is more than that.

The approach is to compare each service from the reference model with the model to test and score it with the Jaccard Index. We then have the clusters with the best similarity. The problem is that there can be different services from the reference model that are paired with the same service from the sample model, as it would falsify the results, we have to remove the duplicates.

The duplicates are removed by picking the duplicate with the lowest score and coupling it with another service with a lower score but that is not already present is the similitude map. We continue this process until there are no duplicates.

We then need to get the mean of the jaccard index between each service pair to have a final similarity between 0 and 1.

## MojoFM

The different architecture are also compared to the ground truth architecture using the MojoFM Metric.

MoJo distance between two clusterings A and B of the same software system is defined as the minimum number of Move or Join operations one needs to perform in order to transform either A  to B or vice versa. The smaller the MoJo distance between an automatically created decomposition A and the “gold standard” decomposition B.
While Mojo gives only a score, MojoFM can give a quality metric by dividing the number of moves and joins to go from A to B to by the highest number of move and join possible to go from any decompisition of B to B.

However MoJoFM assumes that the component sets in the architectures undergoing comparison will be identical, this is unrealistic for most models that are generated so I tweaked it a bit.
This version of MojoFM is based on an implementation by Zhihua Wen. But it also handles classes present in a model but not in the other as a single move operation, it still can't handle duplicate classes really well so if there are duplicated the result will be printed in red and the number of duplicate will be presented. It will be up to the user to consider if the number of duplicates is relevant or not depending on the result and the size of the models.

## A2A

Architecture-to-architecture (a2a) is designed to address some of MoJoFM drawbacks.
MoJoFM’s Join operation is excessively cheap for clusters containing a high number of elements.
MoJoFM also does not properly handle discrepancy of files between the recovered architecture and the ground truth. 

a2a is a distance measure between two architectures aiming to correct some flaws of MojoFM, it takes into account the following operations:

 - AddC (Add a cluster) 
 - RemC (Remove a cluster) 
 - AddE (Add an entity)
 - RemE (Remove an entity)
 - MovE (Move an entity)

It's important to note that any operations like for instance removing an entity takes two steps, first moving the entity out of the cluster and then removing it.

By placing each cluster of each architecture on a side of a bipartite graph and setting the weight of the arcs between each service as the intersection of the content of the two services, we can then apply a maximum weighted bipartite matching to get the best matching possible (meaning the minimum moves possible).

Then, we have a base number of moves. By removing all the matching entities from each architecture we are left with the discrepancy between the two models. It is now easy to compute the number of the four remaining operations. (AddC, RemC, AddE and RemE).

We can then obtain a quality metric by dividing this result by the maximum of moves to go from an empty architecture to each of the given model.

## TurboMQ

TurboMQ is a bit different as it is a metric that doesn't need a ground truth architecture to compare. Instead it needs a metamodel (KDM in this case). With this property, we can even apply TurboMQ to the reference architecture.

The principle of this measure is quite simple, it gives a score based on the cohesion of the clusters versus the coupling between them. In order to compute this values I use a weighted model where each type of dependency between entities has a lower or higher weight depending on the coupling it causes. We need to use this method to have the most precise measure as the metamodel is computed with a callgraph and so there can be a lot of small dependencies between entities.

Even if this measure can be used on a single architecture, it needs to be compared to other measures of the same model since it's not a quality measure, it is juste an arbitrary score. 
It is also important to confront it to other metrics because it tends to prioritize more monolithic architectures to sliced ones. (as they maximize the cohesion and don't have a lot of coupling)
