# GiphyPicky

A ridiculously simple android app that uses the giphy sdk to search for gifs.

If launched by the user it will try and 'share' the selected image via the normal android share way. So, for instance, you can share an animated gif to an app that doesn't have gif selection capability built in.

If launched by another app that wants the user to pick an image file, it will return the selected image to the other app.

It requires a giphy sdk api key so you'll have to sign up for an account on giphy.com and produce an api key.

This app was built primarily to provide an external gif provider to the quik sms app but it can be used for any gif picking and sharing.

here's the quik 'integrated' flow;

use 'attach a photo'

![image](https://github.com/user-attachments/assets/3d5a0644-d488-41ac-8559-7a472db9423d)

select giphy picky to provide the image (this is the only additional step that a fully integrated solution wouldn't require);

![image](https://github.com/user-attachments/assets/1a1f50d6-0009-4220-b5cb-82e399a23ada)

enter your search terms;

![image](https://github.com/user-attachments/assets/81a8e803-c25e-47cc-b6c8-c426ea2a6d5e)

scroll through about a bazillion gifs and click on your choice;

![image](https://github.com/user-attachments/assets/c40a7c6a-90bc-4a61-bed3-16238690506c)

it downloads and gets added to your sms;

![image](https://github.com/user-attachments/assets/235a12b9-326b-4043-b6d1-c7931a430f37)

sharing gifs to other apps too;

launch giphy picky;

![image](https://github.com/user-attachments/assets/c8829173-8199-4239-ad1d-7fc83cff22ee)

enter search and select gif;

![image](https://github.com/user-attachments/assets/078597d9-2866-40dd-be47-5968b52a0816)

android share chooser pops up with whatever apps you have that can take images; such as quik or k9 mail

![image](https://github.com/user-attachments/assets/f5d9aeaf-4a77-4c38-9c1f-f1dc0f9e3be4)

gif gets 'shared' in to the selected app - in this case K9 mail;

![image](https://github.com/user-attachments/assets/ad4f1ea7-d9b3-4c4c-b43c-659956f5dd67)

if you 'share' to quik you'll get the compose with contacts and existing conversations screen;

![image](https://github.com/user-attachments/assets/8ac925c7-555b-4410-959e-bddfce615ef6)

select an existing conversation and the gif gets added to a new sms for that conversation;

![image](https://github.com/user-attachments/assets/31eeea39-fd1d-430c-996b-47c5365a214f)


and an example of picking a giphy gif whilst composing an email in k9 mail;
use the attach icon as you would for any other attachment;

![image](https://github.com/user-attachments/assets/a7055241-e486-484e-b467-8e6e1576e970)

select to get attachment from giphy picky;

![image](https://github.com/user-attachments/assets/c0e8b813-18aa-4368-a25a-df30a42b0804)

select a gif;

![image](https://github.com/user-attachments/assets/adc089e7-c47e-41d1-8b21-78511a2b8c9e)

gif is downloaded and attached to your email;

![image](https://github.com/user-attachments/assets/d122c440-24df-4108-94fe-5f87c15f43bb)
