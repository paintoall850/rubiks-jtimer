Rubik's JTimer - the new JNetCube


To-Do List:

Splash screen redo, the 2 JPGs, needs to include my name, and be in either bright green or bright purple. Hunt was supposed to do this but he’s busy I guess. If I do it (using photoshop) it might take up to 4 hours for both.

+Average of N, for N>2 and N<=13. Much of the back-end is implemented, non of the UI for it is. Estimate 2 additional hours left.

+Generate Rw’. Multi-slice for 4x4 and 5x5. Not much coding but I have no idea what the picking-policy is that WCA uses. It can already do CubeImages that have Rw’ and MES, mes, and xyz. Estimate 6 additional hours left.

+JList feature, either a popup box for it or embedded in the main window. It will offer ability to delete and modify times. The stats-engine is strong enough that it shouldn’t take much coding but is pretty serious feature since the only thing I know about the UI so far is to use a JList. So a lot of interface design work there… Estimated time left: 16 hours.

+Stop on Key-Down. Much of the functionality is provided in TimerArea.java that I created. It will require removing the JButton that used to trigger the events, and remapping the code a bit. It’s practically ready, just a ton of testing left to do after the change. Estimate 3 hours left.

ToolTips, interesting feature to do… hints popup under the cursor when you mouse-over items. Might be helpful, but pretty low on priorities. No idea how much time this will take, but it’s not something I’ve even started investigating.

ScrambleAlg font customization, either or both – color and font face. Not terribly difficult but might be too over-the-top necessary.

+Server/Client stats-engine revamp. This is coupled with the “mm:ss.xx” problems there. Also the AcceptTime button there, “+2” among other things. This will lower the lines of code, I believe. It will make S/C mode much more robust and maintainable. A lot of testing will have to be done afterwards. Estimated time remaining: 10 hours.

Multiple Connects in S/C mode? Haven’t started. Not sure if there’s much interest. No idea what to do for the UI there. Not going to guess how long it will take.

Disconnect Button in Server/Client. This was something Hayes was hinting he would take are of for me when the time came… but I might have to do it and I’d estimate perhaps 30 minutes once I figure out where to place it.

Button to go back to Standalone from Server/Client. The mechanisms are already in place so once I figure out where to place the button, then 15 minutes to do. But this needs to rely on the stuff for Disconnect since we don’t want to switch back leaving the other side hanging… the other side will thin they are being ignored.

Anti-Listening Button. In pre-Server screen when it displays “LISTENING” we need a button to “stop trying”. Estimate 20 minutes to do once I find a place for the button.

Allow Save/Prompt for saving on Disconnection. Kind of a lacking feature… since you have no control over your internet connection or if the other guy suddenly drops and you wanted to see those times/scrambles again. This **should** (not absolutely necessary but should) wait till after we have a Disconnect Button and associated event handling code.

Perhaps Widen Standalone by 80px? This will make it the same width as S/C and so that the ScramblePane lines up identically… perhaps we can then consider having megaminxImage lined up.

TimerThread sleep() for S/C mode… not sure why this wasn’t in there cuz it differes from Stanalone’s way of doing it. Also the “run()” is different and doesn’t encompass the countdown and the “pre-countdown” (Ready…321!) parts.

Add more to S/C’s MenuBar. Such as Import scrambles into Server mode! This is easy after we revamp the stats engine there.

Detachable ScrambleView. Fun thing to do to show off the dynamic auto-scaling capabilities of the ScramblePane JPanel.

Generate **Square-1** ScrambleAlgs. This is horrible, I am having no idea the WCA policy for it. Moderate priority.

Generate **Square-1** Images. This is not too bad, I have most of it planed out and code that detects impossible/wrong scrambles. Low on priorities of course.
