# draw 3d model in ply format on android

## Описание

**Приложение умеет:**

  * читать 3d модель в формате ply;
  * визуализировать эту модель;
  * выполнять трансформацию: **`поворот`**, **`масштабирование`**.
  
**Приложение написано с использванием:**

  * Android Studio 2.3.3;
  * [jPLY library][id1];
  * OpenGL 2.0
  
[id1]:https://github.com/smurn/jPLY

## Источники вдохновения:

1) [Урок 168. OpenGL. Введение][id2] (**[source code][id7]**);
2) [Learn OpenGL ES][id3] (**[source code][id5]**);
3) [LWJGL Game Dev][id4] (**[source code][id6]**).


[id2]:http://startandroid.ru/ru/uroki/vse-uroki-spiskom/397-urok-168-opengl-vvedenie.html
[id3]:http://www.learnopengles.com/android-lesson-eight-an-introduction-to-index-buffer-objects-ibos/
[id4]:https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter5/chapter5.html
[id5]:https://github.com/learnopengles/Learn-OpenGLES-Tutorials/tree/master/android/AndroidOpenGLESLessons/app/src/main/java/com/learnopengles/android
[id6]:https://github.com/lwjglgamedev/lwjglbook
[id7]:https://github.com/startandroid/lessons_opengl

<img src="https://github.com/Muhammadsafarali/OpenGL_Android_Model.ply/blob/master/img1.png" width="200"> <img src="https://github.com/Muhammadsafarali/OpenGL_Android_Model.ply/blob/master/img2.png" width="200"> <img src="https://github.com/Muhammadsafarali/OpenGL_Android_Model.ply/blob/master/img3.png" width="200">

  ***
### Установка jply

```gradle
  compile 'org.smurn:jply:0.2.0'
```
