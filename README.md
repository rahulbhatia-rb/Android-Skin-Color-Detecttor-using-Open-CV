
## Features:
Thus the features can be summarized as follows:
	1. Face detection with real time adaptive filter effect using color detection.
	2. Face Detection with face-only filter.
	3. Real time Encrypt/password protect using color detection. 
### Objective and Applications:
The main objective of our app is to use the on Touch color detection for practical real time-purposes.  
1. Face-detection:													
(1a) On such purpose could be a adaptive filter effect-as of instagram and other such social media. Our feature differs from the naive filters as in, the filters used are generally limited and controlled by the application designers. In our app, it is possible to create filters on-the-go just by clicking on a color on camera interface, providing virtually a broader range of filters.

 (1b) Other implementation of face detector would be changing the color of the face alone keeping remaining portion of the image constant. This is what we actually see in DSLR imaging techniques. In our app, we made a "Avatar"(blue) filter.

2. Password/Encryption:
Other purpose could be using the touch color detection to encrypt/ store the passwords within an image. In general, most of the people use pattern,PIN as passwords, which can be easily hacked. We implemented an idea of such locking implementation using color. Our app has an interface to train, wherein the user is asked to pick random color points from the camera interface or of static colored image with different colors. The touched color, hue, saturations for those  points are stored in a database. When the user, at a later time picks the same colors either from camera interface/static image, they are compared against the database and authenticated. All it requires for the user is to remember the sequence of colors for the password. For better accuracies we stick with static images. 


