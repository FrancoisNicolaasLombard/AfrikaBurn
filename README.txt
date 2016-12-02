*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`
*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`
*`*`*`			Afrika Burn Map			*`*`*`
*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`
*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`*`

//////////////////////////////////////////////////////////////
			Progress
//////////////////////////////////////////////////////////////
-First used Aptana Studio to try and solve the problem using
html, js, and css. The given code was difficult to understand
and unorganised. It was therefore decided to rather restart
the project based on JavaFX, FXML and CSS.
	-A webbased program does not have direct access to
	files on the computer and thus leads to a frustrating
	and redundant process of having to specify files the
	whole time whilst using the program.
-The Netbeans IDE is used rather than IntelliJ, because 
IntelliJ's free version does not support CSS and not using
code completion is redundant if using CSS.
-Scene Builder was downloaded to design the GUI and Netbeans
is used for the logic.
-The latitude and longitude coordinates are read from a JSON
file and then converted to cartesian.
-Implemented features to drag and zoom the map.

//////////////////////////////////////////////////////////////
			Problems
//////////////////////////////////////////////////////////////
-The map is warped and a better approximation is needed to 
ensure the most precise area calculations
-The layering is buggy.

//////////////////////////////////////////////////////////////
			TODO
//////////////////////////////////////////////////////////////
-Import Clients and edit in the program
-Export PDF/PNG file of the map
-Clipping of edges
-Calculating the Areas of polygons
-Save data
-Drag Clients onto polygons
-Display metadata on the bottom left label
-