package com.example.androidmp3project

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.androidmp3project.databinding.MusicViewBinding
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import java.text.SimpleDateFormat

class MyAdapter(val context: Context, val musicData: MutableList<MusicData>): RecyclerView.Adapter<MyAdapter.MyViewHolder>(){
    var ALBUM_IMAGE_SIZE = 80
    val mainActivity: MainActivity = (context as MainActivity)

    val  dbHelper:DBHelper = DBHelper(context, "musicFile.db", 3)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = MusicViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        val myViewHolder = MyViewHolder(binding)

        return myViewHolder
    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
        val binding = holder.binding

        binding.tvArtist.text = musicData?.get(position)?.artist //가수명
        binding.tvTitle.text = musicData?.get(position)?.title
        binding.tvDuration.text = SimpleDateFormat("mm:ss").format(musicData?.get(position)?.duration)

        
        when(musicData[holder.adapterPosition].good){
            0 ->{
                holder.binding.ivGood.setImageResource(R.drawable.good_24)
                binding.ivBad.isEnabled = true

            }
            1 -> {
                holder.binding.ivGood.setImageResource(R.drawable.good_up_24)
                binding.ivBad.isEnabled = false
            }
        }

        when(musicData[holder.adapterPosition].bad){
            0 ->{
                holder.binding.ivBad.setImageResource(R.drawable.bad_24)
                binding.ivGood.isEnabled = true
            }
            1 -> {
                holder.binding.ivBad.setImageResource(R.drawable.bad_down_24)
                binding.ivGood.isEnabled = false
            }
        }

        val bitmap: Bitmap? = musicData?.get(position)?.getAlbumImage(context, ALBUM_IMAGE_SIZE)

        if(bitmap != null){
            binding.ivAlbumArt.setImageBitmap(bitmap)
        }else{
            //앨범이미지가 없을경우 디폴트 이미지를 붙여넣는다.
            binding.ivAlbumArt.setImageResource(R.drawable.person_24)
        }

        binding.ivGood.setOnClickListener {
            when(musicData[position].good){
                0 ->{
                    musicData[position].good = 1
                    if(dbHelper.updateGood(musicData[position])){
                        Toast.makeText(context,"좋아요!!!",Toast.LENGTH_SHORT).show()
                    }
                    notifyItemChanged(position)
                }
                1->{
                    musicData[position].good = 0
                    if(dbHelper.updateGood(musicData[position])){
                        if(mainActivity.typeOfList == Type.Good){
                            musicData.remove(musicData[position])
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }

        binding.ivBad.setOnClickListener {
            when(musicData[position].bad){
                0 ->{
                    musicData[position].bad = 1
                    if(dbHelper.updateBad(musicData[position])){
                        Toast.makeText(context,"나빠요....",Toast.LENGTH_SHORT).show()
                    }
                    notifyItemChanged(position)
                }
                1->{
                    musicData[position].bad = 0
                    if(dbHelper.updateBad(musicData[position])){
                        if(mainActivity.typeOfList == Type.Good){
                            musicData.remove(musicData[position])
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }

        //
        val slidePanel = mainActivity.binding.mainFrame


        //해당 항목을 클릭하면 해당되는 musicData[position] mainActivity 정보애 맞게 수정한다.
        binding.root.setOnClickListener {
            //액티비티 음악정보를 넘겨서 음악을 재생해주는 액티비티 설계하겠다.
            mainActivity.showScreenPlay(musicData[position])
            val state = slidePanel.panelState
            if(state == SlidingUpPanelLayout.PanelState.EXPANDED){
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }

        }
    }

    override fun getItemCount(): Int {
        return musicData?.size ?:0
    }

    class MyViewHolder(val binding: MusicViewBinding): RecyclerView.ViewHolder(binding.root)
}

