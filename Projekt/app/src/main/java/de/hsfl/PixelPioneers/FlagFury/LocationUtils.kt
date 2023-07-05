package de.hsfl.PixelPioneers.FlagFury

import android.location.Location

class LocationUtils {
    companion object {
        private const val tlLatitude = 54.778514
        private const val tlLongitude = 9.442749
        private const val brLatitude = 54.769009
        private const val brLongitude = 9.464722

        fun generatePosition(position: Pair<Double, Double>): Pair<Double, Double> {
            val posX = (position.first - tlLongitude) / (brLongitude - tlLongitude)
            val posY = (position.second - tlLatitude) / (brLatitude - tlLatitude)

            return Pair(posX, posY)
        }

        fun reverseGeneratePosition(posX: Double, posY: Double): Pair<Double, Double> {
            val longitude = posX * (brLongitude - tlLongitude) + tlLongitude
            val latitude = posY * (brLatitude - tlLatitude) + tlLatitude

            return Pair(longitude, latitude)
        }

        fun checkConquerPoint(
            conquerPoint: Point,
            currentPosition: Pair<Double, Double>,
            team: Int
        ): Boolean {
            val playerLocation = Location("Player")
            playerLocation.latitude = currentPosition.second
            playerLocation.longitude = currentPosition.first

            val conquerLocation = Location("ConquerPoint")
            conquerLocation.latitude = conquerPoint.latitude
            conquerLocation.longitude = conquerPoint.longitude

            val distance = playerLocation.distanceTo(conquerLocation)
            return distance < 5 && conquerPoint.team != team
        }

        fun calculateMarkerPosX(
            markerPosition: Pair<Double, Double>,
            mapImageWidth: Int,
            markerViewWidth: Int
        ): Double {
            return (markerPosition.first * mapImageWidth - markerViewWidth / 2)
        }

        fun calculateMarkerPosY(
            markerPosition: Pair<Double, Double>,
            mapImageHeight: Int,
            markerViewHeight: Int
        ): Double {
            return (markerPosition.second * mapImageHeight - markerViewHeight / 2)
        }

    }
}
