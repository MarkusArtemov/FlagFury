package de.hsfl.PixelPioneers.FlagFury

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.hsfl.PixelPioneers.FlagFury.databinding.ListRowBinding
import org.json.JSONObject

class PlayerAdapter(private var playerList: List<JSONObject>, private val clickListener: OnPlayerClickListener, private val isHost: Boolean, private val hostName: String) :
    RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, isHost, hostName)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = playerList[position]
        holder.bind(player, clickListener)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    fun submitList(players: List<JSONObject>) {
        playerList = players
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ListRowBinding, private val isHost: Boolean, private val hostName: String) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(player: JSONObject, clickListener: OnPlayerClickListener) {
            val name = player.getString("name")
            val team = player.getInt("team")

            binding.name.text = name
            binding.team.text = if (team == 1) "Rot" else "Blau"

            val kickButton = binding.button

            if (isHost && name != hostName) {
                kickButton.visibility = View.VISIBLE
                kickButton.setOnClickListener {
                    clickListener.onPlayerClick(player)
                }
            } else {
                kickButton.visibility = View.GONE
            }
        }
    }
}
