package tz.ac.dit.parking.web.dto;

public record OccupancyReport(long totalSlots, long occupiedSlots, long freeSlots,
                              int occupancyPercent, long vehiclesOnSite) {
}
