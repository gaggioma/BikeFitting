package com.example.myposition.components

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.sqrt

// x1,y1 is the center of the first circle, with radius r1
// x2,y2 is the center of the second cricle, with radius r2
fun intersectTwoCircles( x1: Double,y1: Double,r1: Double, x2: Double,y2: Double,r2: Double):List<Pair<Double, Double>> {
    val centerdx = x1 - x2;
    val centerdy = y1 - y2;
    val R = sqrt(centerdx * centerdx + centerdy * centerdy);
    if (!(abs(r1 - r2) <= R && R <= r1 + r2)) { // no intersection
        return emptyList(); // empty list of results
    }
    // intersection(s) should exist

    val R2 = R*R;
    val R4 = R2*R2;
    val a = (r1*r1 - r2*r2) / (2 * R2);
    val r2r2 = (r1*r1 - r2*r2);
    val c = sqrt(2 * (r1*r1 + r2*r2) / R2 - (r2r2 * r2r2) / R4 - 1);

    val fx = (x1+x2) / 2 + a * (x2 - x1);
    val gx = c * (y2 - y1) / 2;
    val ix1 = fx + gx;
    val ix2 = fx - gx;

    val fy = (y1+y2) / 2 + a * (y2 - y1);
    val gy = c * (x1 - x2) / 2;
    val iy1 = fy + gy;
    val iy2 = fy - gy;

    // note if gy == 0 and gx == 0 then the circles are tangent and there is only one solution
    // but that one solution will just be duplicated as the code is currently written
    return listOf(ix1 to iy1, ix2 to iy2)
}

fun getColorBy(score: Float): Color{

    if(score > 97f && score <= 100f){
        return Color(0xFF44CE1B)
    }else

    if(score > 94f && score <= 97f){
        return Color(0xFFBBDB44)
    }else

    if(score > 88f && score <=94f){
        return Color(0xFFF7E379)
    }else

    if(score >76f && score <= 88f){
        return Color(0xFFF2A134)
    }else{
        return Color(0xFFE51F1F)
    }
}