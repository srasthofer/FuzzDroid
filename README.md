# FuzzDroid

## Abstract
Android applications, or apps, provide useful features to end-users, but many apps also contain malicious behavior. Modern malware makes understanding such behavior challenging by behaving maliciously only under particular conditions. For example, a malware app may check whether it runs on a real device and not an emulator, in a particular country, and alongside a specific target app, such as a vulnerable banking app. To observe the malicious behavior, a security analyst must find out and emulate all these app-specific constraints. This paper presents FuzzDroid, a framework for automatically generating an Android execution environment where an app exposes its malicious behavior. The key idea is to combine an extensible set of static and dynamic analyses through a search-based algorithm that steers the app toward a configurable target location. On recent malware, the approach reaches the target location in 75% of the apps. In total, we reach 240 code locations within an average time of only one minute. To reach these code locations, FuzzDroid generates 106 different environments, too many for a human analyst to create manually.

## Publication 
```
@inproceedings{fuzzdroid,
	author = {Siegfried Rasthofer and Steven Arzt and Stefan Triller and Michael Pradel},
	title = {Making Malory Behave Maliciously: Targeted Fuzzing of Android Execution Environments},
 	booktitle = {Proceedings of the 39th International Conference on Software Engineering (ICSE)},
 	year = {2017},
 	publisher = {ACM}
}
```
