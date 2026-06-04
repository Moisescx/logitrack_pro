package org.proyecto.logistica.service;

public class GpsService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calcula la distancia en km entre dos coordenadas GPS usando la fórmula de Haversine
     */
    public static double calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 0.0;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Valida que las coordenadas GPS estén dentro de rangos válidos
     */
    public static boolean validarCoordenadas(Double latitud, Double longitud) {
        if (latitud == null || longitud == null) {
            return false;
        }
        // Latitud: -90 a 90, Longitud: -180 a 180
        return latitud >= -90 && latitud <= 90 && longitud >= -180 && longitud <= 180;
    }

    /**
     * Valida que la velocidad sea razonable (0 a 200 km/h)
     */
    public static boolean validarVelocidad(Double velocidad) {
        if (velocidad == null) {
            return true; // Opcional
        }
        return velocidad >= 0 && velocidad <= 200;
    }
}
