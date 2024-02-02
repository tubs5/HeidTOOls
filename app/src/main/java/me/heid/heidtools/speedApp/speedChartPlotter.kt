package me.heid.heidtools.speedApp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import me.heid.heidtools.R


class speedChartPlotter : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_chart_plotter)
        setSupportActionBar(findViewById(R.id.toolbar))

        val anyChartView = findViewById<AnyChartView>(R.id.any_chart_view)

        val cartesian: Cartesian = AnyChart.line()

        cartesian.animation(true)

        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)

        cartesian.title("DATA")

        cartesian.yAxis(0).title("Speed(km/h)")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)


        val seriesData: MutableList<DataEntry> = ArrayList()
        /* seriesData.add(CustomDataEntry("1986", 3.6, 2.3, 2.8))
         seriesData.add(CustomDataEntry("1987", 7.1, 4.0, 4.1))
         seriesData.add(CustomDataEntry("1988", 8.5, 6.2, 5.1))
         seriesData.add(CustomDataEntry("1989", 9.2, 11.8, 6.5))
         seriesData.add(CustomDataEntry("1990", 10.1, 13.0, 12.5))
         seriesData.add(CustomDataEntry("1991", 11.6, 13.9, 18.0))
         seriesData.add(CustomDataEntry("1992", 16.4, 18.0, 21.0))
         seriesData.add(CustomDataEntry("1993", 18.0, 23.3, 20.3))
         seriesData.add(CustomDataEntry("1994", 13.2, 24.7, 19.2))
         seriesData.add(CustomDataEntry("1995", 12.0, 18.0, 14.4))
         seriesData.add(CustomDataEntry("1996", 3.2, 15.1, 9.2))
         seriesData.add(CustomDataEntry("1997", 4.1, 11.3, 5.9))
         seriesData.add(CustomDataEntry("1998", 6.3, 14.2, 5.2))
         seriesData.add(CustomDataEntry("1999", 9.4, 13.7, 4.7))
         seriesData.add(CustomDataEntry("2000", 11.5, 9.9, 4.2))
         seriesData.add(CustomDataEntry("2001", 13.5, 12.1, 1.2))
         seriesData.add(CustomDataEntry("2002", 14.8, 13.5, 5.4))
         seriesData.add(CustomDataEntry("2003", 16.6, 15.1, 6.3))
         seriesData.add(CustomDataEntry("2004", 18.1, 17.9, 8.9))
         seriesData.add(CustomDataEntry("2005", 17.0, 18.9, 10.1))
         seriesData.add(CustomDataEntry("2006", 16.6, 20.3, 11.5))
         seriesData.add(CustomDataEntry("2007", 14.1, 20.7, 12.2))
         seriesData.add(CustomDataEntry("2008", 15.7, 21.6, 10))
         seriesData.add(CustomDataEntry("2009", 12.0, 22.5, 8.9))*/




        val set: Set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping: Mapping = set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping: Mapping = set.mapAs("{ x: 'x', value: 'value2' }")
        val series3Mapping: Mapping = set.mapAs("{ x: 'x', value: 'value3' }")

        val series1: Line = cartesian.line(series1Mapping)
        series1.name("Brandy")
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        anyChartView.setChart(cartesian)
    }
}

private class CustomDataEntry internal constructor(
    x: String?,
    value: Number?,
    value2: Number?,
    value3: Number?
) :
    ValueDataEntry(x, value) {
    init {
        setValue("value2", value2)
        setValue("value3", value3)
    }
}