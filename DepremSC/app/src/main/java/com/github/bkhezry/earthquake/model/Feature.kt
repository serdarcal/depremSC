package com.github.bkhezry.earthquake.model


import android.graphics.Color
import android.os.Parcelable
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.listener.CardClickListener
import com.github.bkhezry.earthquake.util.AppUtil
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.card.MaterialCardView
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Feature(
  @SerializedName("type")
  val typeString: String,
  @SerializedName("properties")
  val properties: Properties,
  @SerializedName("geometry")
  val geometry: Geometry,
  @SerializedName("id")
  val id: String
) : ClusterItem, AbstractItem<Feature.ViewHolder>(), Parcelable {
  override fun getViewHolder(v: View): ViewHolder {
    return ViewHolder(v)
  }

  override val type: Int
    get() = R.id.fastadapter_sample_item_id

  /** defines the layout which will be used for this item in the list  */
  override val layoutRes: Int
    get() = R.layout.feature_item


  override fun getSnippet(): String {
    return ""
  }

  override fun getTitle(): String {
    return ""
  }

  /**
   * Get coordinates of feature as LatLng
   *
   * @return LatLng
   */
  override fun getPosition(): LatLng {
    return LatLng(geometry.coordinates[1], geometry.coordinates[0])
  }

  /**
   * Handle view of Feature items
   *
   * @property view MaterialCardView
   * @property infoButton ImageButton
   * @property title TextView for showing title of earthquake
   * @property mag TextView for showing mag of earthquake
   * @property date TextView for showing date of earthquake
   * @constructor
   */
  class ViewHolder(view: View) : FastAdapter.ViewHolder<Feature>(view) {
    val view: MaterialCardView = view as MaterialCardView
    var infoButton: ImageButton = view.findViewById(R.id.info_button)
    var title: TextView = view.findViewById(R.id.title_text_view)
    var mag: TextView = view.findViewById(R.id.mag_text_view)
    var date: TextView = view.findViewById(R.id.date_text_view)
    override fun bindView(item: Feature, payloads: MutableList<Any>) {
      view.clearAnimation()
      if (item.isSelected) {
        val color: Int = Color.argb(255, 255, 255, 51)
        view.strokeColor = color
        view.strokeWidth = AppUtil.dpToPx(1f)
        view.foreground = FastAdapterUIUtils.getSelectablePressedBackground(
          view.context,
          FastAdapterUIUtils.adjustAlpha(color, 20),
          50,
          true
        )
      }
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = item.properties.time
      val format = SimpleDateFormat("E, MMM d, yyyy  hh:mm", Locale.getDefault())
      val stringArray = item.properties.place.split("of")
      if (stringArray.size == 2) {
        title.text = stringArray[1]
      }
      mag.text = view.context.getString(R.string.mag_label)
        .plus(
          String.format(
            "%.1f",
            item.properties.mag
          )
        )
      date.text = format.format(calendar.time)

    }

    override fun unbindView(item: Feature) {
      title.text = null
      mag.text = null
      date.text = null
      view.strokeColor = Color.WHITE
      view.strokeWidth = AppUtil.dpToPx(0f)
    }
  }

  /**
   * Handle click on the feature item buttons
   * return selected item with position of it with the CardClickListener interface
   *
   * @property listener [CardClickListener]
   * @constructor
   */
  class InfoFabClickEvent(cardClickListener: CardClickListener) : ClickEventHook<Feature>() {
    private var listener: CardClickListener = cardClickListener
    override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
      return if (viewHolder is ViewHolder) {
        viewHolder.infoButton
      } else null
    }

    override fun onClick(
      v: View,
      position: Int,
      fastAdapter: FastAdapter<Feature>,
      item: Feature
    ) {
      if (!item.isSelected) {
        val selectExtension: SelectExtension<Feature> = fastAdapter.requireExtension()
        val selections = selectExtension.selections
        if (selections.isNotEmpty()) {
          for (selectedPosition in selections.iterator()) {
            selectExtension.deselect(selectedPosition)
            fastAdapter.notifyAdapterItemChanged(selectedPosition)
          }
        }
        selectExtension.select(position)
        fastAdapter.notifyAdapterItemChanged(position)

      }
      AppUtil.vibrate()
      listener.selected(item, position)
    }
  }

}