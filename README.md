Let me preface this with a simple statement, I am not a java dev! I've been experimetning with 
different ways to represent comlex ag data in S2 and have built this to help out. 

The only bit I may merge back is the term indexer code which I've ported from the (Actually 
maintained) C++ library

# WallyS2

For compiled Jars see [here](https://adaptris.atlassian.net/wiki/spaces/ODS/pages/1525448741/Wally+S2+realease). You don't need
to pull this to use it, or understand the code. You just need to get how you access the useful functionality. 

Note that the code under `com.google.common.geometry` is from Google's implementation and should thus remain open source. 
Please remember this as you play with it and don't add anything confidential. In the future we can import that from 
maven and extend a couple of classes to achieve what we want to achieve. I just needed to hold the code for a 
bit. `org.proagrica.wallys2` is all me.

This is a bit of a beast of a program so I'll walk you though it as best I can. If you want to run something from this
"live" then you can load it up in intelliJ, it's smart enough that it'll load all the dependencies for you using Maven 
but if not just run the POM file to get all the dependencies in. You can then run any script as you would in PyCharm. 
If you make changes and want to rebuild then you have to go to Project Structure > Artefacts and setup an artefact 
build that includes all dependencies. You can then Build > Artefact to produce a new Jar.  

Alternatively you can use the compiled jar under the out directory, you can then import this into a different java project 
or run bits from the command line. You run something like `java -cp path_to_wallys2.jar class.to.call.main.of`, for 
example: `java -cp wallys2.jar org.proagrica.wallys2.gateways.CellEntryPoint` to boot the Java/Python gateway. 

## Rebuilding 

## Google functions
Check the original github [here](https://github.com/google/s2-geometry-library-java), I've mainly kept it all as is but
I've added a couple of ease of use functions and changes some types to make it easier for us to use. 

## Wally
### Converters

#### coordConverter
This is used to get m2 and m representations of a WKT, after converting to a target geometry so we can actually get 
meters. It uses the US National Atlas as a default but it can be altered as needed. It's more "proper" Java in htat it's 
an object that's instansiated. 

#### wktConverter
Functions to convert to and from S2 and WKTs. For the most part you'll be using `wktToS2()` but there's also 
an `s2ToWkt()` if needed. 

### gateways
These allow us to boot a java gateway that Python can access, in order to use the region coverer functions. See the 
relevant function in [pyagrica](https://gitlab.agb.rbxd.ds/DataScience/pyagrica) which will allow you to extract the 
determined area. As with the csv tool it also checks for oddities in the polygons but unlike that tool it will not even 
try to adjust the polygon. That's your job I'm afraid. 

Boot the gateway from IntelliJ or you can use: 
`java -cp wallys2.jar org.proagrica.wallys2.gateways.CellEntryPoint`

The functions you then have access to are given in `org.proagrica.wallys2.gateways.CellProcessor` but you can theoretically
access anything that the S2CellUnion object has. See the python docs for info. 

### Utils 
#### CellIdTools
Generalised tools for handling lists of cell IDs. Under the bonnet what this does is take a list of
`Long` type numbers, convert them to cell unions and then apply an S2Cell function on them. Usually
something like an intersection check or similar. It's largely used for Spark integration. 

#### CellUnionTools
As above for S2CellUnions (polygons)

#### GeometryTools
Wally also contains a standard geo toolset, allowing WKTs to be read to geometroes and then checked for validity, area,
intersections and the like. THis is where they live. 

#### S2GeomAreaTools
Calculating the area of an S2 cell is actually quite hard. This class solves this problem the easy way by converting
the cell union to a WKT and then measuring its area. Currently hard coded to use the US National Atlas as the projection
of choice. 

#### S2CellAreaTools
Calculating the area of an S2 cell is actually quite hard. They are calculated as the area of a sphere of radius one
so need multiplying by the radius of the Earth at that point on the globe (the Earth isn't a sphere). This class 
handles that so doesn't require the cast to a geometry that the geom area tools do. It usually gives a pretty solid
answer but it's rarely identical to the WKT approach.  