package com.o2dialer.app.ui.components

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp

/** Lucide 0.546.0 paths, ISC licensed; same 24px viewport and 1.75px stroke as the reference. */
object O2Icons {
    val Phone: ImageVector by lazy { ImageVector.Builder("Phone",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M13.832 16.568a1 1 0 0 0 1.213-.303l.355-.465A2 2 0 0 1 17 15h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2A18 18 0 0 1 2 4a2 2 0 0 1 2-2h3a2 2 0 0 1 2 2v3a2 2 0 0 1-.8 1.6l-.468.351a1 1 0 0 0-.292 1.233 14 14 0 0 0 6.392 6.384").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Chart: ImageVector by lazy { ImageVector.Builder("Chart",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M5 21v-6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 21V3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M19 21V9").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Megaphone: ImageVector by lazy { ImageVector.Builder("Megaphone",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M11 6a13 13 0 0 0 8.4-2.8A1 1 0 0 1 21 4v12a1 1 0 0 1-1.6.8A13 13 0 0 0 11 14H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M6 14a12 12 0 0 0 2.4 7.2 2 2 0 0 0 3.2-2.4A8 8 0 0 1 10 14").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 6v8").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Clock: ImageVector by lazy { ImageVector.Builder("Clock",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 6v6l4 2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 2 12 a 10 10 0 1 0 20 0 a 10 10 0 1 0 -20 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Settings: ImageVector by lazy { ImageVector.Builder("Settings",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 9 12 a 3 3 0 1 0 6 0 a 3 3 0 1 0 -6 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Globe: ImageVector by lazy { ImageVector.Builder("Globe",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M 2 12 a 10 10 0 1 0 20 0 a 10 10 0 1 0 -20 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M2 12h20").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Search: ImageVector by lazy { ImageVector.Builder("Search",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("m21 21-4.34-4.34").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 3 11 a 8 8 0 1 0 16 0 a 8 8 0 1 0 -16 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Plus: ImageVector by lazy { ImageVector.Builder("Plus",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M5 12h14").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 5v14").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val More: ImageVector by lazy { ImageVector.Builder("More",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M 11 12 a 1 1 0 1 0 2 0 a 1 1 0 1 0 -2 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 18 12 a 1 1 0 1 0 2 0 a 1 1 0 1 0 -2 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 4 12 a 1 1 0 1 0 2 0 a 1 1 0 1 0 -2 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Play: ImageVector by lazy { ImageVector.Builder("Play",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Pause: ImageVector by lazy { ImageVector.Builder("Pause",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M15 3 H18 Q19 3 19 4 V20 Q19 21 18 21 H15 Q14 21 14 20 V4 Q14 3 15 3 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M6 3 H9 Q10 3 10 4 V20 Q10 21 9 21 H6 Q5 21 5 20 V4 Q5 3 6 3 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Skip: ImageVector by lazy { ImageVector.Builder("Skip",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M21 4v16").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M6.029 4.285A2 2 0 0 0 3 6v12a2 2 0 0 0 3.029 1.715l9.997-5.998a2 2 0 0 0 .003-3.432z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Stop: ImageVector by lazy { ImageVector.Builder("Stop",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M5 3 H19 Q21 3 21 5 V19 Q21 21 19 21 H5 Q3 21 3 19 V5 Q3 3 5 3 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Trash: ImageVector by lazy { ImageVector.Builder("Trash",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M10 11v6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M14 11v6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M3 6h18").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Edit: ImageVector by lazy { ImageVector.Builder("Edit",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m15 5 4 4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Share: ImageVector by lazy { ImageVector.Builder("Share",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M 15 5 a 3 3 0 1 0 6 0 a 3 3 0 1 0 -6 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 3 12 a 3 3 0 1 0 6 0 a 3 3 0 1 0 -6 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 15 19 a 3 3 0 1 0 6 0 a 3 3 0 1 0 -6 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8.59 13.51 L15.42 17.49").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M15.41 6.51 L8.59 10.49").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Reset: ImageVector by lazy { ImageVector.Builder("Reset",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M3 3v5h5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Users: ImageVector by lazy { ImageVector.Builder("Users",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M16 3.128a4 4 0 0 1 0 7.744").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M22 21v-2a4 4 0 0 0-3-3.87").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 5 7 a 4 4 0 1 0 8 0 a 4 4 0 1 0 -8 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Check: ImageVector by lazy { ImageVector.Builder("Check",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M20 6 9 17l-5-5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Chevron: ImageVector by lazy { ImageVector.Builder("Chevron",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("m6 9 6 6 6-6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Back: ImageVector by lazy { ImageVector.Builder("Back",24.dp,24.dp,24f,24f, autoMirror = true).apply {
        addPath(PathParser().parsePathString("m12 19-7-7 7-7").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M19 12H5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Upload: ImageVector by lazy { ImageVector.Builder("Upload",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 3v12").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m17 8-5-5-5 5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Copy: ImageVector by lazy { ImageVector.Builder("Copy",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M10 8 H20 Q22 8 22 10 V20 Q22 22 20 22 H10 Q8 22 8 20 V10 Q8 8 10 8 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Tag: ImageVector by lazy { ImageVector.Builder("Tag",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 7 7.5 a 0.5 0.5 0 1 0 1 0 a 0.5 0.5 0 1 0 -1 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Message: ImageVector by lazy { ImageVector.Builder("Message",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M2.992 16.342a2 2 0 0 1 .094 1.167l-1.065 3.29a1 1 0 0 0 1.236 1.168l3.413-.998a2 2 0 0 1 1.099.092 10 10 0 1 0-4.777-4.719").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Filter: ImageVector by lazy { ImageVector.Builder("Filter",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M10 5H3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 19H3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M14 3v4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M16 17v4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M21 12h-9").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M21 19h-5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M21 5h-7").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 10v4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 12H3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val PhoneCall: ImageVector by lazy { ImageVector.Builder("PhoneCall",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M13 2a9 9 0 0 1 9 9").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M13 6a5 5 0 0 1 5 5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M13.832 16.568a1 1 0 0 0 1.213-.303l.355-.465A2 2 0 0 1 17 15h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2A18 18 0 0 1 2 4a2 2 0 0 1 2-2h3a2 2 0 0 1 2 2v3a2 2 0 0 1-.8 1.6l-.468.351a1 1 0 0 0-.292 1.233 14 14 0 0 0 6.392 6.384").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Close: ImageVector by lazy { ImageVector.Builder("Close",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M18 6 6 18").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m6 6 12 12").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Timer: ImageVector by lazy { ImageVector.Builder("Timer",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M10 2 L14 2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 14 L15 11").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 4 14 a 8 8 0 1 0 16 0 a 8 8 0 1 0 -16 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Refresh: ImageVector by lazy { ImageVector.Builder("Refresh",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M21 3v5h-5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 16H3v5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Person: ImageVector by lazy { ImageVector.Builder("Person",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 8 7 a 4 4 0 1 0 8 0 a 4 4 0 1 0 -8 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Forward: ImageVector by lazy { ImageVector.Builder("Forward",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M5 12h14").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m12 5 7 7-7 7").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Chat: ImageVector by lazy { ImageVector.Builder("Chat",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M2.992 16.342a2 2 0 0 1 .094 1.167l-1.065 3.29a1 1 0 0 0 1.236 1.168l3.413-.998a2 2 0 0 1 1.099.092 10 10 0 1 0-4.777-4.719").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val CheckCircle: ImageVector by lazy { ImageVector.Builder("CheckCircle",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M 2 12 a 10 10 0 1 0 20 0 a 10 10 0 1 0 -20 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m9 12 2 2 4-4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Error: ImageVector by lazy { ImageVector.Builder("Error",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M 2 12 a 10 10 0 1 0 20 0 a 10 10 0 1 0 -20 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 8 L12 12").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 16 L12.01 16").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Info: ImageVector by lazy { ImageVector.Builder("Info",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M 2 12 a 10 10 0 1 0 20 0 a 10 10 0 1 0 -20 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 16v-4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 8h.01").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Label: ImageVector by lazy { ImageVector.Builder("Label",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 7 7.5 a 0.5 0.5 0 1 0 1 0 a 0.5 0.5 0 1 0 -1 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Star: ImageVector by lazy { ImageVector.Builder("Star",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val ThumbDown: ImageVector by lazy { ImageVector.Builder("ThumbDown",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M17 14V2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M9 18.12 10 14H4.17a2 2 0 0 1-1.92-2.56l2.33-8A2 2 0 0 1 6.5 2H20a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-2.76a2 2 0 0 0-1.79 1.11L12 22a3.13 3.13 0 0 1-3-3.88Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Voicemail: ImageVector by lazy { ImageVector.Builder("Voicemail",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M 2 12 a 4 4 0 1 0 8 0 a 4 4 0 1 0 -8 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 14 12 a 4 4 0 1 0 8 0 a 4 4 0 1 0 -8 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M6 16 L18 16").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Warning: ImageVector by lazy { ImageVector.Builder("Warning",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 9v4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 17h.01").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val VerifiedUser: ImageVector by lazy { ImageVector.Builder("VerifiedUser",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m9 12 2 2 4-4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Contacts: ImageVector by lazy { ImageVector.Builder("Contacts",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M16 2v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M7 22v-2a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 2v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 9 11 a 3 3 0 1 0 6 0 a 3 3 0 1 0 -6 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M5 4 H19 Q21 4 21 6 V20 Q21 22 19 22 H5 Q3 22 3 20 V6 Q3 4 5 4 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Mic: ImageVector by lazy { ImageVector.Builder("Mic",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 19v3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M19 10v2a7 7 0 0 1-14 0v-2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 2 H12 Q15 2 15 5 V12 Q15 15 12 15 H12 Q9 15 9 12 V5 Q9 2 12 2 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val MicOff: ImageVector by lazy { ImageVector.Builder("MicOff",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 19v3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M15 9.34V5a3 3 0 0 0-5.68-1.33").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M16.95 16.95A7 7 0 0 1 5 12v-2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M18.89 13.23A7 7 0 0 0 19 12v-2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m2 2 20 20").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M9 9v3a3 3 0 0 0 5.12 2.12").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val CallReceived: ImageVector by lazy { ImageVector.Builder("CallReceived",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M16 2v6h6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m22 2-6 6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M13.832 16.568a1 1 0 0 0 1.213-.303l.355-.465A2 2 0 0 1 17 15h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2A18 18 0 0 1 2 4a2 2 0 0 1 2-2h3a2 2 0 0 1 2 2v3a2 2 0 0 1-.8 1.6l-.468.351a1 1 0 0 0-.292 1.233 14 14 0 0 0 6.392 6.384").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val ChevronRight: ImageVector by lazy { ImageVector.Builder("ChevronRight",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("m9 18 6-6-6-6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val PhoneMissed: ImageVector by lazy { ImageVector.Builder("PhoneMissed",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("m16 2 6 6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m22 2-6 6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M13.832 16.568a1 1 0 0 0 1.213-.303l.355-.465A2 2 0 0 1 17 15h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2A18 18 0 0 1 2 4a2 2 0 0 1 2-2h3a2 2 0 0 1 2 2v3a2 2 0 0 1-.8 1.6l-.468.351a1 1 0 0 0-.292 1.233 14 14 0 0 0 6.392 6.384").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Notifications: ImageVector by lazy { ImageVector.Builder("Notifications",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M10.268 21a2 2 0 0 0 3.464 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Security: ImageVector by lazy { ImageVector.Builder("Security",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val VolumeUp: ImageVector by lazy { ImageVector.Builder("VolumeUp",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M16 9a5 5 0 0 1 0 6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M19.364 18.364a9 9 0 0 0 0-12.728").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val SystemUpdate: ImageVector by lazy { ImageVector.Builder("SystemUpdate",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 15V3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m7 10 5 5 5-5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val ArrowDownward: ImageVector by lazy { ImageVector.Builder("ArrowDownward",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 5v14").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m19 12-7 7-7-7").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val ArrowUpward: ImageVector by lazy { ImageVector.Builder("ArrowUpward",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("m5 12 7-7 7 7").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 19V5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val CheckBox: ImageVector by lazy { ImageVector.Builder("CheckBox",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M5 3 H19 Q21 3 21 5 V19 Q21 21 19 21 H5 Q3 21 3 19 V5 Q3 3 5 3 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m9 12 2 2 4-4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val CheckBoxOutlineBlank: ImageVector by lazy { ImageVector.Builder("CheckBoxOutlineBlank",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M5 3 H19 Q21 3 21 5 V19 Q21 21 19 21 H5 Q3 21 3 19 V5 Q3 3 5 3 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val FolderOpen: ImageVector by lazy { ImageVector.Builder("FolderOpen",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("m6 14 1.5-2.9A2 2 0 0 1 9.24 10H20a2 2 0 0 1 1.94 2.5l-1.54 6a2 2 0 0 1-1.95 1.5H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.69.9l.81 1.2a2 2 0 0 0 1.67.9H18a2 2 0 0 1 2 2v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val GroupRemove: ImageVector by lazy { ImageVector.Builder("GroupRemove",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 5 7 a 4 4 0 1 0 8 0 a 4 4 0 1 0 -8 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M22 11 L16 11").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Insights: ImageVector by lazy { ImageVector.Builder("Insights",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 16v5").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M16 14v7").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M20 10v11").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m22 3-8.646 8.646a.5.5 0 0 1-.708 0L9.354 8.354a.5.5 0 0 0-.707 0L2 15").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M4 18v3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 14v7").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Sim: ImageVector by lazy { ImageVector.Builder("Sim",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 14v4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M14.172 2a2 2 0 0 1 1.414.586l3.828 3.828A2 2 0 0 1 20 7.828V20a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 14h8").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M9 10 H15 Q16 10 16 11 V17 Q16 18 15 18 H9 Q8 18 8 17 V11 Q8 10 9 10 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Palette: ImageVector by lazy { ImageVector.Builder("Palette",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 22a1 1 0 0 1 0-20 10 9 0 0 1 10 9 5 5 0 0 1-5 5h-2.25a1.75 1.75 0 0 0-1.4 2.8l.3.4a1.75 1.75 0 0 1-1.4 2.8z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 13 6.5 a 0.5 0.5 0 1 0 1 0 a 0.5 0.5 0 1 0 -1 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 17 10.5 a 0.5 0.5 0 1 0 1 0 a 0.5 0.5 0 1 0 -1 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 6 12.5 a 0.5 0.5 0 1 0 1 0 a 0.5 0.5 0 1 0 -1 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 8 7.5 a 0.5 0.5 0 1 0 1 0 a 0.5 0.5 0 1 0 -1 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val TrendingUp: ImageVector by lazy { ImageVector.Builder("TrendingUp",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M16 7h6v6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m22 7-8.5 8.5-5-5L2 17").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Smartphone: ImageVector by lazy { ImageVector.Builder("Smartphone",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M7 2 H17 Q19 2 19 4 V20 Q19 22 17 22 H7 Q5 22 5 20 V4 Q5 2 7 2 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 18h.01").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val BookOpen: ImageVector by lazy { ImageVector.Builder("BookOpen",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M12 7v14").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val FileCode: ImageVector by lazy { ImageVector.Builder("FileCode",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M4 22h14a2 2 0 0 0 2-2V7l-5-5H6a2 2 0 0 0-2 2v4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M14 2v4a2 2 0 0 0 2 2h4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m5 12-3 3 3 3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m9 18 3-3-3-3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val PersonAdd: ImageVector by lazy { ImageVector.Builder("PersonAdd",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 5 7 a 4 4 0 1 0 8 0 a 4 4 0 1 0 -8 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M19 8 L19 14").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M22 11 L16 11").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val ClipboardPaste: ImageVector by lazy { ImageVector.Builder("ClipboardPaste",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M11 14h10").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M16 4h2a2 2 0 0 1 2 2v1.344").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m17 18 4-4-4-4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 4H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 1.793-1.113").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M9 2 H15 Q16 2 16 3 V5 Q16 6 15 6 H9 Q8 6 8 5 V3 Q8 2 9 2 Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val FileUp: ImageVector by lazy { ImageVector.Builder("FileUp",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M14 2v4a2 2 0 0 0 2 2h4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 12v6").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("m15 15-3-3-3 3").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val BookUser: ImageVector by lazy { ImageVector.Builder("BookUser",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M15 13a3 3 0 1 0-6 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H19a1 1 0 0 1 1 1v18a1 1 0 0 1-1 1H6.5a1 1 0 0 1 0-5H20").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 10 8 a 2 2 0 1 0 4 0 a 2 2 0 1 0 -4 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Spreadsheet: ImageVector by lazy { ImageVector.Builder("Spreadsheet",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M14 2v4a2 2 0 0 0 2 2h4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 13h2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M14 13h2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M8 17h2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M14 17h2").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Bolt: ImageVector by lazy { ImageVector.Builder("Bolt",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val Sparkles: ImageVector by lazy { ImageVector.Builder("Sparkles",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594l-5.558-1.051a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M20 2v4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M22 4h-4").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M 2 20 a 2 2 0 1 0 4 0 a 2 2 0 1 0 -4 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
    val AlertCircle: ImageVector by lazy { ImageVector.Builder("AlertCircle",24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString("M 2 12 a 10 10 0 1 0 20 0 a 10 10 0 1 0 -20 0").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 8 L12 12").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
        addPath(PathParser().parsePathString("M12 16 L12.01 16").toNodes(),fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build() }
}
