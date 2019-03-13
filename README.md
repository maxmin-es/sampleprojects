This repo contains sample projects that I've worked on in the past.

CS32 - contains code from three projects that I built in Brown University's Software Engineering course. The /main/main.java
       holds the calls to start all three programs: stars, AutoCorrect, and Bacon. The main code for each of these projects
       is found in their respective directories.
       
Stars: after loading corpus of 3D points in space, stored in CSV file, user can call "radius" or "neighbor" of either
       a given star name or a set of 3 coordinates. Neighbor returns the k nearest neighbors ("neighbor x y z k" in terminal)
       while radius returns other points in a given radius, floating-point r ("radius x y z r" in terminal). Also functional
       via web-based GUI. 
       
AutoCorrect: Users load in corpus from .txt file. Words are parsed, stored in trie and maps of unigram and bigram frequency
       User can then, through the web-based GUI, type and recieve recommendations for auto predictions. Predictions calculated
       using edit-distance, prefix, whitespace (word is split in all combinations to see if user forgot space).
       
Bacon: reads from .sqlite3 file a database of actors -> id, a database of films -> id, and a database of films -> 
       actors in film. User can then through a web-based GUI explore shortest path between certain actors. Users can also
       click name of actor to see all movies, or name of film to see all actor.
