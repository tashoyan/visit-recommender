package com.github.tashoyan.visitor.recommender

import com.github.tashoyan.visitor.recommender.Location._

case class Location(latitude: Double, longitude: Double) {
  require(latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE, s"Latitude $latitude must be within range [$MIN_LATITUDE, $MAX_LATITUDE]")
  require(longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE, s"Longitude $longitude must be within range [$MIN_LONGITUDE, $MAX_LONGITUDE]")

  def distanceMeters(that: Location): Double = {
    Location.distanceMeters(this, that)
  }
}

//TODO FastMath from commons-math3
object Location {
  val MIN_LATITUDE: Double = -90.0
  val MAX_LATITUDE: Double = 90.0
  val MIN_LONGITUDE: Double = -180.0
  val MAX_LONGITUDE: Double = 180.0

  val earthRadiusMeters: Double = 6371 * 1000

  def distanceMeters(location1: Location, location2: Location): Double = {
    val lat1 = math.toRadians(location1.latitude)
    val lat2 = math.toRadians(location2.latitude)
    val lon1 = math.toRadians(location1.longitude)
    val lon2 = math.toRadians(location2.longitude)
    val hav = haversine(lat2 - lat1) +
      math.cos(lat1) * math.cos(lat2) * haversine(lon2 - lon1)
    earthRadiusMeters * 2 * math.sqrt(hav)
  }

  def haversine(theta: Double): Double = {
    val s = math.sin(theta / 2)
    s * s
  }

}
