package com.backoffice.fitandflex.entity;

public enum ReservationStatus {
    ACTIVE,    // Reserva activa
    CANCELED,  // Cancelada por el usuario o administrador
    ATTENDED,  // El usuario asistió a la clase
    NO_SHOW    // El usuario no asistió
}
