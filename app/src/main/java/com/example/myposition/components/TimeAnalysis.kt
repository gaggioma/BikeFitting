package com.example.myposition.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myposition.components.models.analysisModel
import com.example.myposition.views.viewModel.models.frameModel
import com.example.myposition.views.viewModel.models.makeAngleAnalysis
import kotlin.math.abs

@Composable
fun TimeAnalysis(
    frames: List<frameModel>,
    currentFrame: frameModel?
){
    val ySeries = remember { mutableStateListOf<MutableList<Float?>>() }
    val xLabels = remember { mutableStateListOf<Float>() }
    val titles = remember { mutableStateListOf<String>() }
    val maxY = remember { mutableStateListOf<Float>() }
    val minY = remember { mutableStateListOf<Float>() }
    val outOfRangeList = remember { mutableStateListOf<analysisModel>() }

    //Update series, titles and labels when frame list have updated
    LaunchedEffect(frames) {

        //Clear label and series
        xLabels.clear()
        ySeries.clear()
        maxY.clear()
        minY.clear()
        titles.clear()
        outOfRangeList.clear()

        //Init phase
        frames.forEachIndexed { frameId, frame ->

            //Labels
            xLabels.add(frameId.toFloat())

            //Titles and series
            if(titles.size < frame.boxInfoList.size){

                //Title for ever landmark
                titles.clear()
                frame.boxInfoList.forEach {
                    titles.add(it.name)
                }

                //Init series for every landmark
                ySeries.clear()
                outOfRangeList.clear()
                frame.boxInfoList.forEachIndexed { serieId, landmarkModel ->
                    ySeries.add(mutableListOf())
                    /*outOfRangeList.add(
                        analysisModel(
                        id= landmarkModel.name
                    )
                    )*/
                }

                //Init max and min Y for every landmark
                maxY.clear()
                minY.clear()
                frame.boxInfoList.forEachIndexed { serieId, landmarkModel ->
                    landmarkModel.maxAngle?.let { maxY.add(it) }
                    landmarkModel.minAngle?.let { minY.add(it) }
                }

            }
        }

        //Add serie values
        frames.forEachIndexed { frameId, frame ->

            if(frame.boxInfoList.isEmpty()){
                for(serieId in 0..< ySeries.size){
                    ySeries[serieId].add(null)
                }
            }else {
                frame.boxInfoList.forEachIndexed { serieId, landmarkModel ->
                    //Add sample into serie
                    ySeries[serieId].add(landmarkModel.currentAngle)
                }
            }
        }

        //Score analysis
        val scoreAnalysis = makeAngleAnalysis(frameList = frames)
        outOfRangeList.addAll(scoreAnalysis)

    }

    LazyColumn {
        titles.forEachIndexed { index, name ->
            item(key = name) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp).padding(5.dp)
                ) {

                        //Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        //Out of range analysis
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Samples analysis: ",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .drawWithCache {
                                            onDrawBehind {
                                                drawCircle(
                                                    color = getColorBy(score = outOfRangeList[index].score),
                                                    radius = 20f)
                                            }
                                        }
                                        .height(20.dp)
                                        .padding(start = 10.dp),
                                )
                            }

                            Text(
                                text = "Total score: ${String.format("%.1f", outOfRangeList[index].score)}%, positive: ${outOfRangeList[index].positiveSamples}, tot: ${outOfRangeList[index].totSamples}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }


                        val nullValue = currentFrame?.boxInfoList?.size!! < titles.size
                        LineChart(
                            ySerie = ySeries[index],
                            xLabels = xLabels,
                            currentX = currentFrame.frameId.toFloat(),
                            currentY = if (nullValue) 0f else currentFrame.boxInfoList.get(index).currentAngle,
                            maxY = maxY.get(index),
                            minY = minY.get(index)
                        )
                }
            }
        }
    }
}