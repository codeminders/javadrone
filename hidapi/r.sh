#!/bin/sh

java -Djava.library.path=/usr/local/cuda/lib:.:/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:/Users/$USER/src/ardrone/hidapi/jni-impl -classpath bin com.codeminders.hidapi.HIDAPITest

#rm -f r.log
#sudo dtruss java -Djava.library.path=/usr/local/cuda/lib:.:/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:/Users/$USER/src/ardrone/hidapi/jni-impl -classpath bin com.codeminders.hidapi.HIDAPITest >r.log 2>&1 
