# FAQ #

**Q:** Does it work with AR.Drone 2.0

**A:** The development team has not tried, as we do not have the new drone yet.


---


**Q:** My PS3 controller does not work

**A:** You need to press PS3 button once to activate it.


---


**Q:** Is your code cross platform/portable

**A:** Yes, API code and demo program are portable. However to work with PS3 controller we using native [JavaHID](http://code.google.com/p/javahidapi/) library, interface via JNI. The library is ported to MacOS, Linux and Windows.


---


**Q:** Do I need Parrot SDK to use your API

**A:** No. Our API is completely independent and self-contained.