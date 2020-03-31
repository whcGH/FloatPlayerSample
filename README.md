# FloatPlayerSample
悬浮窗播放实现样例

### 主要功能
+ 悬浮窗播放视频
+ 点击悬浮窗进入全屏播放页面,并关闭悬浮窗
+ 点击悬浮窗右上角关闭悬浮窗

### 具体实现
为了保持悬浮窗和全屏播放页面视频播放同步，该样例使用一个Service来运行播放器，并在其Binder中提供方法来生成SurfaceView以显示视频内容  
在PlayerService中，主要进行播放器的初始化和各种Listener的设置  
Binder中，除了提供创建SurfaceView的方法外，还提供一些操作播放器的基础方法（设置监听器、设置资源文件、开始、暂停、结束等），以及跳转页面的方法（Service中跳转Activity需要加FLAG_ACTIVITY_NEW_TASK）  
  
MainActivity中，首先绑定Service，在获得Binder后，通过Binder设置视频资源  
FloatWindow初始化后，通过ApplicationContext绑定Service，在获取Binder后进行相关设置，并获取SurfaceView添加到悬浮窗预留的fl_container内  
点击FloatWindow窗体，通过Binder调用PlayerService跳转Activity的方法，跳转到全屏播放页  
进入全屏播放页后，首先应当绑定PlayerService，再关闭FloatWindow(关闭FloatWindow时FloatWindow创建时绑定的Service会解除绑定)  

### 备注
+ 由于业务需要，视频播放器使用了阿里云播放器的SDK
+ 悬浮窗使用的是[FloatWindow](https://github.com/yhaolpz/FloatWindow)
