package com.example.androidmp3project

import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidmp3project.databinding.ActivityMainBinding
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    private var mediaPlayer: MediaPlayer? = null
    private var musicData: MusicData? = null
    private var currentMusic: MusicData? = null
    var playMusicDataList: MutableList<MusicData>? = mutableListOf()
    var musicDataList: MutableList<MusicData>? = mutableListOf()
    var searchMusicDataList: MutableList<MusicData>? = mutableListOf()
    private var musicJob : Job? = null
    val dbHelper: DBHelper = DBHelper(this, "musicFile.db", 3)
    var typeOfList = Type.ALL
    var Playing = false
    var FABMainStatus = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //외부파일 읽기 승인요청
        fun isPermitted(): Boolean{
            if(ContextCompat.checkSelfPermission(this, permission[0])!= PackageManager.PERMISSION_GRANTED){
                return false
            }else{
                return true
            }
        }

        //승인이 되었으면 음악파일을 가져오는 것이고, 승인이 안되어있으면 다시 재요청
        if(isPermitted() == true){
            startProcess()
        }else{
            //승인 요청을 다시 해야한다. android.Manifest.permission.READ_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(this, permission, 200)
        }



        //FAB바를 누르면 FAB버튼들이 올라오게 하는 애니메이션
        binding.FABMain.setOnClickListener {
            toggle()
        }

        //FABSelect 터치했을때 모든정보가 리사이클러뷰에 뿌려짐
        binding.FABSelect.setOnClickListener {
            musicDataList = dbHelper.selectAllMusic()
            binding.recyclerView.adapter = MyAdapter(this, musicDataList!!)
        }

        //FABGood 터치했을때 좋아요 누른 정보만 리사이클러뷰에 뿌려짐
        binding.FABGood.setOnClickListener {
            musicDataList = dbHelper.selectGood()
            binding.recyclerView.adapter = MyAdapter(this, musicDataList!!)
        }

        //FABBad 터치했을때 나빠요 누른 정보만 리사이클러뷰에 뿌려짐
        binding.FABBad.setOnClickListener {
            musicDataList = dbHelper.selectBad()
            binding.recyclerView.adapter = MyAdapter(this, musicDataList!!)
        }

        //데코레이션
        binding.recyclerView.addItemDecoration(MyDecoration(this))

        //뷰셋팅
        if(musicData != null){
            binding.tvTitle.text = musicData?.title
            binding.tvArtist.text = musicData?.artist
            binding.tvSeekBarStart.text = "00:00"
            binding.tvSeekBarEnd.text = SimpleDateFormat("mm:ss").format(musicData?.duration)
            val bitmap: Bitmap? = musicData?.getAlbumImage(this, 250)
            if(bitmap != null){
                binding.coverImageView.setImageBitmap(bitmap)
            }else{
                binding.coverImageView.setImageResource(R.drawable.person_24)
            }

            //음원실행 생성및 재생
            mediaPlayer = MediaPlayer.create(this, musicData?.getMusicFileUri())
            binding.seekBar.max = musicData?.duration!!.toInt()


            //시크바 이벤트 설정을 해서 노래와 같이 동기화 처리 된다.
            binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                //시크바를 터치하고 이동할때 발생돼는 이벤트 fromUser: 유저에 의한 터치 유무
                override fun onProgressChanged(seekBar: SeekBar?, progerss: Int, fromUser: Boolean) {
                    if(fromUser){
                        //미디어 플레이어에 음악위치를 시크바에서 값을 가져와서 셋팅한다
                        mediaPlayer?.seekTo(progerss)
                    }
                }
                //시크바를 터치하는 순간 이벤트 발생
                override fun onStartTrackingTouch(p0: SeekBar?) {

                }
                //시크바를 터치하고 놓는순간 이벤트를 발생
                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
        }


        val slidePanel = binding.mainFrame                     // SlidingUpPanel
        slidePanel.addPanelSlideListener(PanelEventListener())  // 이벤트 리스너 추가

        // 패널 열고 닫기
        binding.tvOpen.setOnClickListener {
            val state = slidePanel.panelState
            // 닫힌 상태일 경우 열기
            if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            }
            // 열린 상태일 경우 닫기
            else if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
        }

        //시크바 이벤트 설정을 해서 노래와 같이 동기화 처리 된다.
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, process: Int, fromUser: Boolean) {
                if(fromUser){
                    //미디어 플레이어에 음악위치를 시크바에서 값을 가져와서 셋팅한다
                    mediaPlayer?.seekTo(process)
                    //시크바 움직인만큼 진행된 tvSeekBarStart.text도 같이 초기화 시켜준다
                    binding.tvSeekBarStart.text = SimpleDateFormat("mm:ss").format(process)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)

        val searchMenu = menu?.findItem(R.id.search_menu)
        val searchView = searchMenu?.actionView as SearchView

        //서뷰를 이벤트 리스너
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(p0: String?): Boolean {
                var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)

                return true
            }

            //글자를 입력할때 제목과 아티스트이름이 비슷하거나 같으면 리사이클러뷰에 뿌려줌
            override fun onQueryTextChange(s: String?): Boolean {
                if(!s.isNullOrBlank()){
                    searchMusicDataList = dbHelper.selectMusic(s, s)
                    Log.d("shin", "${s}")
                    binding.recyclerView.adapter = MyAdapter(this@MainActivity, searchMusicDataList!!)
                    Log.d("shin","${searchMusicDataList}")
                }else{
                    binding.recyclerView.adapter = MyAdapter(this@MainActivity, musicDataList!!)
                    searchMusicDataList?.clear()
                }
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }

    private fun startProcess(){
        //DB에서 데이터 자료를 가져옴
        musicDataList = dbHelper.selectAllMusic()

        //만역에 데이터 자료가 없다면 musicDataList에 데이터를 저장
        if(musicDataList == null || musicDataList!!.size <= 0){
            musicDataList = getMusicList()

            for(i in 0..musicDataList!!.size- 1){
                val musicData = musicDataList?.get(i)
                if (dbHelper.insertMusic(musicData!!) == false){
                    Log.d("shin", "삽입오류 ${musicData.toString()}")
                    Log.d("shin", "${musicDataList.toString()}")
                }
            }
            Log.d("shin","테이블에 없어서 ...getMusicList()")
        }else{
//            Log.d("shin","테이블에 없어서 getMusicList()")
//            Log.d("shin", "${musicDataList}")
        }
        val myAdapter = MyAdapter(this, musicDataList!!)
        binding.recyclerView.adapter = myAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }


    private fun getMusicList(): MutableList<MusicData>? {
        //1. MP3 외부파일에 있는 음원 정보 주소
        val listUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        val cursor = contentResolver.query(listUri, projection,null, null, null)
        val musicDataList: MutableList<MusicData>? = mutableListOf<MusicData>()
        while(cursor?.moveToNext() == true){
            val id = cursor.getString(0)
            val title = cursor.getString(1).replace("'"," ")
            var artist = cursor.getString(2).replace("'"," ")
            var albumId = cursor.getString(3)
            var duration = cursor.getInt(4)

            val musicData= MusicData(id, title, artist, albumId, duration, 0, 0)
            musicDataList?.add(musicData)
        }
        cursor?.close()
        return musicDataList
    }

    // SlidingUpPanelLayout 이벤트 리스너
    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener {
        // 패널이 슬라이드 중일 때
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
        }

        // 패널의 상태가 변했을 때
        override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                binding.tvOpen.text = "△"

            } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                binding.tvOpen.text = "▽"
            }
        }
    }

    //플로팅 액션버튼 이벤트
    private fun toggle() {
        if(FABMainStatus) {
            // 플로팅 액션 버튼 닫기
            // 애니메이션 추가
            val FABBadGoAnimation = ObjectAnimator.ofFloat(binding.FABBad, "translationY", 0f)
            FABBadGoAnimation.start()
            val FABGoodAnimation = ObjectAnimator.ofFloat(binding.FABGood, "translationY", 0f)
            FABGoodAnimation.start()
            val FABSelectGoAnimation = ObjectAnimator.ofFloat(binding.FABSelect, "translationY", 0f)
            FABSelectGoAnimation.start()
            // 메인 플로팅 이미지 변경
            binding.FABMain.setImageResource(R.drawable.next_24)
        }else {
            // 플로팅 액션 버튼 열기
            val FABBadGoAnimation = ObjectAnimator.ofFloat(binding.FABBad, "translationY", -200f)
            FABBadGoAnimation.start()
            val FABGoodAnimation = ObjectAnimator.ofFloat(binding.FABGood, "translationY", -400f)
            FABGoodAnimation.start()
            val FABSelectGoAnimation = ObjectAnimator.ofFloat(binding.FABSelect, "translationY", -600f)
            FABSelectGoAnimation.start()
            // 메인 플로팅 이미지 변경
            binding.FABMain.setImageResource(R.drawable.less_24)
        }
        // 플로팅 버튼 상태 변경
        FABMainStatus = !FABMainStatus
    }
    // view의 터치 이벤트
    fun onClickView(view: View?){
        if(currentMusic != null){
            when(view?.id){
                R.id.ivPlay ->{
                    if(mediaPlayer?.isPlaying == true){
                        musicPlayerPause()
                    }else{
                        musicPlayerStart()
                    }
                }//end of ivPlay
                R.id.ivNext ->{
                    musicPlayerNext()
                }
                R.id.ivBefore ->{
                    musicPlayerBefore()
                }
            }
        }
    }

    //노래 재생화면

    ///////////////////////////////////////////////////
    //노래 보여줄 화면
    fun showScreenPlay(musicData: MusicData){
        musicPlayerStop()

        if(searchMusicDataList!!.isEmpty()){
            playMusicDataList = musicDataList
            currentMusic = musicDataList!![musicDataList!!.indexOf(musicData)]
        }else{
            playMusicDataList = searchMusicDataList
            currentMusic = playMusicDataList!![playMusicDataList!!.indexOf(musicData)]
        }
        musicPlayerUpdate(musicData)
        mediaPlayer = MediaPlayer.create(this, musicData.getMusicFileUri())

        /*val musicDataList = dbHelper.selectAllMusic()

        playMusicDataList = musicDataList
        var music = playMusicDataList?.get(musicDataList?.indexOf(musicData)!!)
        currentMusic = music
        musicPlayerUpdate(currentMusic!!)
        mediaPlayer = MediaPlayer.create(this, musicData?.getMusicFileUri())*/

        if(mediaPlayer?.isPlaying == false){
            musicPlayerStart()
        }
    }

    //노래파일 수정
    fun musicPlayerUpdate(musicData: MusicData){
        val bitmap: Bitmap? = musicData?.getAlbumImage(this, 250)
        if(bitmap != null){
            binding.coverImageView.setImageBitmap(bitmap)
        }else{
            binding.coverImageView.setImageResource(R.drawable.ic_baseline_music_note_24)
        }
        binding.tvArtist.text = musicData.artist
        binding.tvTitle.text = musicData.title
        binding.tvSeekBarEnd.text = SimpleDateFormat("mm:ss").format(musicData.duration)
        binding.seekBar.max = musicData.duration ?: 0
    }

    //노래 일시 정지
    fun musicPlayerPause(){
        mediaPlayer?.pause()
        Playing = false
        binding.ivPlay.setImageResource(R.drawable.play_arrow_24)
    }

    // 노래 정지
    fun musicPlayerStop(){
        mediaPlayer?.stop()
        musicJob?.cancel()
        mediaPlayer = null
        binding.seekBar.progress = 0
        binding.tvSeekBarStart.text = "00:00"
        binding.ivPlay.setImageResource(R.drawable.play_arrow_24)
    }

    //노래 시작
    fun musicPlayerStart(){
        mediaPlayer?.start()
        Playing = true
        binding.ivPlay.setImageResource(R.drawable.pause_24)

        val backgroundScope = CoroutineScope(Dispatchers.Default + Job())

        musicJob = backgroundScope.launch {

            while(mediaPlayer?.isPlaying == true){
                runOnUiThread{
                    var currentPosition = mediaPlayer?.currentPosition!!
                    binding.seekBar.progress = currentPosition
                    binding.tvSeekBarStart.text = SimpleDateFormat("mm:ss").format(currentPosition)
                }
                try{
                    delay(500)
                }catch (e:Exception){
                    e.toString()
                }
            }//while of end
            runOnUiThread{
                if(mediaPlayer!!.currentPosition >= (binding.seekBar.max - 10000)){
                    musicPlayerNext()
                }
            }
        }
    }

    //노래 다음곡 이동
    fun musicPlayerNext(){
        if(playMusicDataList!!.isEmpty()){
            Toast.makeText(this,"다음곡 넘어갑니다", Toast.LENGTH_SHORT).show()
            return
        }

        musicPlayerStop()

        var nextMusic: MusicData? = null

        if(playMusicDataList?.indexOf(currentMusic) != playMusicDataList?.lastIndex){
            nextMusic = playMusicDataList!![playMusicDataList?.indexOf(currentMusic)!! + 1]
            mediaPlayer = MediaPlayer.create(this,nextMusic.getMusicFileUri())
            binding.seekBar.max = nextMusic?.duration ?: 0
        }else{
            nextMusic = playMusicDataList?.first()
            mediaPlayer = MediaPlayer.create(this, nextMusic?.getMusicFileUri())
            binding.seekBar.max = nextMusic?.duration ?: 0
        }
        if (nextMusic != null){
            currentMusic = nextMusic
            musicPlayerUpdate(nextMusic)
        }
        if(Playing){
            musicPlayerStart()
        }

    }

    //노래 이전곡으로 이동
    fun musicPlayerBefore(){
        if(playMusicDataList!!.isEmpty()){
            return
        }
        when(mediaPlayer?.currentPosition){
            in 0..5000 ->{
                musicPlayerStop()

                var beForeMusic: MusicData?= null

                if(playMusicDataList?.indexOf(currentMusic) != 0){
                    beForeMusic = playMusicDataList!![playMusicDataList!!.indexOf(currentMusic) - 1]
                    mediaPlayer = MediaPlayer.create(this, beForeMusic.getMusicFileUri())
                    binding.seekBar.max = beForeMusic.duration ?: 0
                }

                if(beForeMusic != null){
                    currentMusic = beForeMusic
                    musicPlayerUpdate(beForeMusic)
                }
            }
            else ->{
                musicPlayerStop()
                mediaPlayer = MediaPlayer.create(this, currentMusic?.getMusicFileUri())
            }
        }
        if(Playing){
            musicPlayerStart()
        }
    }


}


