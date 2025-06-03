package com.example.myposition.views.viewModel.models

import androidx.compose.ui.geometry.Offset
import com.example.myposition.components.intersectTwoCircles
import com.example.myposition.components.models.analysisModel
import com.google.mediapipe.tasks.components.containers.Landmark
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

//Pose landmarks map: https://ai.google.dev/edge/mediapipe/solutions/vision/pose_landmarker#pose_landmarker_model
fun getAllLandmarks(): Map<Int, String>{
    val map = mutableMapOf<Int, String>()

    map[0] = "nose"
    map[1] = "left eye (inner)"
    map[2] = "left eye"
    map[3] = "left eye (outer)"
    map[4] = "right eye (inner)"
    map[5] = "right eye"
    map[6] = "right eye (outer)"
    map[7] = "left ear"
    map[8] = "right ear"
    map[9] = "mouth (left)"
    map[10] = "mouth (right)"
    map[11] = "left shoulder"
    map[12] = "right shoulder"
    map[13] = "left elbow"
    map[14] = "right elbow"
    map[15] = "left wrist"
    map[16] = "right wrist"
    map[17] = "left pinky"
    map[18] = "right pinky"
    map[19] = "left index"
    map[20] = "right index"
    map[21] = "left thumb"
    map[22] = "right thumb"
    map[23] = "left hip"
    map[24] = "right hip"
    map[25] = "left knee"
    map[26] = "right knee"
    map[27] = "left ankle"
    map[28] = "right ankle"
    map[29] = "left heel"
    map[30] = "right heel"
    map[31] = "left foot index"
    map[32] = "right foot index"
    return map
}

fun getLandmarkName(index: Int) : String? {
    return getAllLandmarks()[index]
}

fun getLandmarkIdByName(name: String) : Int {
    return getAllLandmarks().filterValues { it == name }.keys.first()
}

fun getLandmarkAngleList(): List<landmarkModel>{
    return listOf(
        landmarkModel(current = 11, prev = 13, next = 23, minAngle = 65f, maxAngle = 75f),
        landmarkModel(current = 13, prev = 11, next = 15, minAngle = 120f, maxAngle = 160f),
        landmarkModel(current = 23, next = 25, prev = 11, minAngle = 60f, maxAngle = 110f),
        landmarkModel(current = 25, next = 23, prev = 27, minAngle = 65f, maxAngle = 145f),
        landmarkModel(current = 27, next = 31, prev = 25, minAngle = 95f, maxAngle = 180f),

        landmarkModel(current = 12, next = 14, prev = 24, minAngle = 65f, maxAngle = 75f),
        landmarkModel(current = 14, prev = 12, next = 16, minAngle = 120f, maxAngle = 160f),
        landmarkModel(current = 24, next = 26, prev = 12, minAngle = 60f, maxAngle = 110f),
        landmarkModel(current = 26, next = 28, prev = 24, minAngle = 65f, maxAngle = 145f),
        landmarkModel(current = 28, next = 32, prev = 26, minAngle = 95f, maxAngle = 180f)
    )
}

fun getLeftInterestList() : List<Int> {
    return listOf(
        12,
        14,
        16,
        24,
        26,
        28,
        32
    )
}

fun getRightInterestList() : List<Int> {
    return listOf(
        11,
        13,
        15,
        23,
        25,
        27,
        31
    )
}

fun moveSaddleX(
    originList: List<NormalizedLandmark>,
    saddleXShift: Float = 0f
): MutableList<NormalizedLandmark>{

    //Result empty list
    val resultList = mutableListOf<NormalizedLandmark>()

    originList.forEachIndexed { index, normalizedLandmark ->

        //Shift hip, knee according with saddle shift
        if (
            index == getLandmarkIdByName("left ankle") ||
            index == getLandmarkIdByName("right ankle") ||
            index == getLandmarkIdByName("left shoulder") ||
            index == getLandmarkIdByName("right shoulder")
        ){

            //ankle angle = f(knee, foot index, R1, _x, knee_y, R2).  27, 28(left ankle, right ankle), 23,24 (left hip, right hip)
            var id1: Int = 0
            var id2: Int = 0
            var R1: Float = 0f
            var R2: Float = 0f

            if(index == getLandmarkIdByName("left ankle")){
                id1 = getLandmarkIdByName("left knee")
                id2 = getLandmarkIdByName("left foot index")
            }

            if(index == getLandmarkIdByName("right ankle")){
                id1 = getLandmarkIdByName("right knee")
                id2 = getLandmarkIdByName("right foot index")
            }

            if (index == getLandmarkIdByName("left shoulder")) {
                id1 = getLandmarkIdByName("left hip")
                id2 = getLandmarkIdByName("left elbow")
            }

            if (index == getLandmarkIdByName("right shoulder")) {
                id1 = getLandmarkIdByName("right hip")
                id2 = getLandmarkIdByName("right elbow")
            }

            //Maintain the same distance between points
            R1 = offsetDistance(
                p1 = Offset(x = originList[id1].x(), y = originList[id1].y()),
                p2 = Offset(x = originList[index].x(), y = originList[index].y())
            )
            R2 = offsetDistance(
                p1 = Offset(x = originList[id2].x(), y = originList[id2].y()),
                p2 = Offset(x = originList[index].x(), y = originList[index].y())
            )

            var x1 = (originList[id1].x() + saddleXShift).toDouble()
            var y1 = originList[id1].y().toDouble()

            val intesectionList = intersectTwoCircles(
                x1 = x1,
                y1 = y1,
                r1 = R1.toDouble(),
                x2 = originList[id2].x().toDouble(),
                y2 = originList[id2].y().toDouble(),
                r2 = R2.toDouble()
            )

            //If right take 1. If left take 0
            var solution_index: Int = 1

            if(getLandmarkName(index)?.contains("left shoulder") == true){
                solution_index = 1
            }

            if(getLandmarkName(index)?.contains("left ankle") == true){
                solution_index = 0
            }

            if(getLandmarkName(index)?.contains("right shoulder") == true){
                solution_index = 0
            }

            if(getLandmarkName(index)?.contains("right ankle") == true){
                solution_index = 1
            }

            if(intesectionList.isNotEmpty()) {
                //Get the best result between two solutions
                val x: Float = intesectionList[solution_index].first.toFloat()
                val y: Float = intesectionList[solution_index].second.toFloat()
                resultList.add(NormalizedLandmark.create(x, y, normalizedLandmark.z()))
            }else{
                resultList.add(normalizedLandmark)
            }

        }else if (
            index == getLandmarkIdByName("left wrist") ||
            index == getLandmarkIdByName("right wrist") ||
            index == getLandmarkIdByName("left foot index") ||
            index == getLandmarkIdByName("right foot index") ||
            index == getLandmarkIdByName("right elbow") ||
            index == getLandmarkIdByName("left elbow")

        ){
            //Lock these landmarks
            resultList.add(normalizedLandmark)
        }else{//Shift otherwise
            resultList.add(NormalizedLandmark.create(normalizedLandmark.x() + saddleXShift, normalizedLandmark.y(), normalizedLandmark.z()))
        }
    }
    return resultList
}

fun moveSaddleY(
    originList: List<NormalizedLandmark>,
    saddleYShift: Float = 0f
): MutableList<NormalizedLandmark>{

    //Result empty list
    val resultList = mutableListOf<NormalizedLandmark>()

    originList.forEachIndexed { index, normalizedLandmark ->

        //Shift hip, knee according with saddle shift
        if (
            index == getLandmarkIdByName("left knee") ||
            index == getLandmarkIdByName("right knee")
            //index == getLandmarkIdByName("left elbow") ||
            //index == getLandmarkIdByName("right elbow")
        ){

            //ankle angle = f(knee, foot index, R1, _x, knee_y, R2).  27, 28(left ankle, right ankle), 23,24 (left hip, right hip)
            var id1: Int = 0
            var id2: Int = 0
            var R1: Float = 0f
            var R2: Float = 0f

            if(index == getLandmarkIdByName("left knee")){
                id1 = getLandmarkIdByName("left hip")
                id2 = getLandmarkIdByName("left ankle")
            }

            if(index == getLandmarkIdByName("right knee")){
                id1 = getLandmarkIdByName("right hip")
                id2 = getLandmarkIdByName("right ankle")
            }

            if (index == getLandmarkIdByName("left elbow")) {
                id1 = getLandmarkIdByName("left shoulder")
                id2 = getLandmarkIdByName("left wrist")
            }

            if (index == getLandmarkIdByName("right elbow")) {
                id1 = getLandmarkIdByName("right shoulder")
                id2 = getLandmarkIdByName("right wrist")
            }

            R1 = offsetDistance(
                p1 = Offset(x = originList[id1].x(), y = originList[id1].y()),
                p2 = Offset(x = originList[index].x(), y = originList[index].y())
            )
            R2 = offsetDistance(
                p1 = Offset(x = originList[id2].x(), y = originList[id2].y()),
                p2 = Offset(x = originList[index].x(), y = originList[index].y())
            )

            val intesectionList = intersectTwoCircles(
                x1 = originList[id1].x().toDouble(),
                y1 = (originList[id1].y() + saddleYShift).toDouble(),
                r1 = R1.toDouble(),
                x2 = originList[id2].x().toDouble(),
                y2 = originList[id2].y().toDouble(),
                r2 = R2.toDouble()
            )

            //If right take 1. If left take 0
            val solution_index: Int

            if(
                index == getLandmarkIdByName("left knee") ||
                index == getLandmarkIdByName("right elbow")
            ){
                solution_index = 1
            } else{
                solution_index = 0
            }

            if(intesectionList.isNotEmpty()) {
                //Get the best result between two solutions
                val x: Float = intesectionList[solution_index].first.toFloat()
                val y: Float = intesectionList[solution_index].second.toFloat()
                resultList.add(NormalizedLandmark.create(x, y, normalizedLandmark.z()))
            }else{
                resultList.add(normalizedLandmark)
            }
        }else if (
            index == getLandmarkIdByName("left wrist") ||
            index == getLandmarkIdByName("right wrist") ||
            index == getLandmarkIdByName("left foot index") ||
            index == getLandmarkIdByName("right foot index") ||
            index == getLandmarkIdByName("right shoulder") ||
            index == getLandmarkIdByName("left shoulder") ||
            index == getLandmarkIdByName("right elbow") ||
            index == getLandmarkIdByName("left elbow")
        ){
            //Lock these landmarks
            resultList.add(normalizedLandmark)
        }else{//Shift otherwise
            resultList.add(NormalizedLandmark.create(normalizedLandmark.x(), normalizedLandmark.y() + saddleYShift, normalizedLandmark.z()))
        }
    }
    return resultList
}


fun getDirection(
    originList: List<NormalizedLandmark>,
): String?{

    val shoulderLeft = originList[getLandmarkIdByName("left shoulder")]
    val shoulderRight = originList[getLandmarkIdByName("right shoulder")]

    val hipLeft = originList[getLandmarkIdByName("left hip")]
    val hipRight = originList[getLandmarkIdByName("right hip")]

    if(shoulderLeft.x() < hipLeft.x() ) {
        return "left"
    }

    if(shoulderRight.x() > hipRight.x()){
        return "right"
    }

    return null
}

fun getDirectionList(
    originList: List<NormalizedLandmark>,
): List<Int>{

    val direction = getDirection(originList)

    if(direction == "left") {
        return getRightInterestList()
    }

    if(direction == "right"){
        return getLeftInterestList()
    }

    return emptyList()
}

fun offsetDistance(p1: Offset, p2: Offset): Float{
    return sqrt(  abs(p1.x - p2.x).pow(2) + abs(p1.y - p2.y).pow(2))
}

fun getDistanceForPixel(
    originList: List<NormalizedLandmark>,
    worldList: List<Landmark>,
    imgScaleValue: Float,
    imageWidth: Int,
    imageHeight: Int
): Float{
    val direction = getDirection(originList) ?: return 0f

    var hipLandmarkName: String = ""
    var kneeLandmarkName: String = ""

    if(direction == "left"){
        hipLandmarkName = "right hip"
        kneeLandmarkName = "right knee"
    }

    if(direction == "right"){
        hipLandmarkName = "left hip"
        kneeLandmarkName = "left knee"
    }

    //compute distance in [m] between right hip and right knee
    val hipWorld = worldList[getLandmarkIdByName(hipLandmarkName)]
    val kneeWorld = worldList[getLandmarkIdByName(kneeLandmarkName)]
    val offsetWorldHip = Offset(x= hipWorld.x(), y = hipWorld.y())
    val offsetWorldKnee = Offset(x = kneeWorld.x(), y = kneeWorld.y())
    val distanceWorld = offsetDistance(p1 = offsetWorldHip, p2 = offsetWorldKnee)

    //compute distance in pixed from right hip and right knee
    val hip = originList[getLandmarkIdByName(hipLandmarkName)]
    val knee = originList[getLandmarkIdByName(kneeLandmarkName)]
    val offsetHip = Offset(x= hip.x() * imageWidth, y = hip.y() * imageHeight) * imgScaleValue
    val offsetKnee = Offset(x = knee.x() * imageWidth, y = knee.y() * imageHeight) * imgScaleValue
    val pixedDistance = offsetDistance(p1 = offsetHip, p2 = offsetKnee)

    return if(pixedDistance == 0f) 0f else distanceWorld / pixedDistance
}

fun getDistanceCm(
    p1: Offset,
    p2: Offset,
    meterForPixel: Float
): Int{
    //Distance in pixel
    val distancePixel = offsetDistance(p1 = p1, p2 = p2)
    return (distancePixel * meterForPixel * 100).toInt()
}

fun getAction(sequenceCounter : Int): saddleModel{

    //First configuration is reset
    if(sequenceCounter == 0){
        return saddleModel(saddleXShift = 0f, saddleYShift = 0f)
    }

    //Get random action between:
    //0 = up, 1=down, 2=left, 3=right
    //val actionId = Random.nextInt(from = 0, until = 3)
    val actionId = sequenceCounter % 4
    if(actionId == 0){
        return saddleModel(saddleXShift = 0f, saddleYShift = -0.005f, direction = "up")
    }

    if(actionId == 1){
        return saddleModel(saddleXShift = 0f, saddleYShift = +0.005f, direction = "down")
    }

    if(actionId == 2){
        return saddleModel(saddleXShift = -0.005f, saddleYShift = 0f, direction = "left")
    }

    if(actionId == 3){
        return saddleModel(saddleXShift = +0.005f, saddleYShift = 0f, direction = "right")
    }

    return saddleModel(saddleXShift = 0f, saddleYShift = 0f)
}

fun makeAngleAnalysis(frameList: List<frameModel>): List<analysisModel>{

    val outOfRangeList = mutableListOf<analysisModel>()
    val maxY = mutableListOf<Float>()
    val minY = mutableListOf<Float>()

    //Init phase
    frameList.forEachIndexed { frameId, frame ->

        if(!frame.boxInfoList.isEmpty()) {

            outOfRangeList.clear()
            maxY.clear()
            minY.clear()
            frame.boxInfoList.forEachIndexed { serieId, landmarkModel ->

                outOfRangeList.add(
                    analysisModel(
                        id = landmarkModel.name
                    )
                )

                landmarkModel.maxAngle?.let { maxY.add(it) }
                landmarkModel.minAngle?.let { minY.add(it) }
            }
        }
    }

    //Add values
    frameList.forEachIndexed { frameId, frame ->

        frame.boxInfoList.forEachIndexed { serieId, landmarkModel ->

            if(landmarkModel.currentAngle != null){

                //If current angle is inside threshold add positive sample
                if(landmarkModel.currentAngle <= maxY[serieId] && landmarkModel.currentAngle >= minY[serieId]){
                    outOfRangeList[serieId].positiveSamples++
                }

                outOfRangeList[serieId].totSamples++
                outOfRangeList[serieId].score = outOfRangeList[serieId].positiveSamples.toFloat() / outOfRangeList[serieId].totSamples.toFloat() * 100f
            }
        }
    }
     return outOfRangeList
}

fun isMaximum(current: List<analysisModel>, next: List<analysisModel> ): Boolean{

    val errorMarginPercent = 0

    current.forEachIndexed { index, analysisModel ->
        val currentScore = analysisModel.score
        val nextScore = next[index].score
        if(nextScore < currentScore - errorMarginPercent){
            return false
        }
    }

    return true
}