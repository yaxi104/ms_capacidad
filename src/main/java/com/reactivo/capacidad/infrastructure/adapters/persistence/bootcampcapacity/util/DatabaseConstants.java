package com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.util;

public final class DatabaseConstants {

    private DatabaseConstants() {
    }

    // Columnas
    public static final String COLUMN_ID_CAPACITY = "id_capacity";
    public static final String COLUMN_ID_BOOTCAMP = "id_bootcamp";

    // Queries
    public static final String PAGED_BOOTCAMP_IDS_QUERY = """
            SELECT id_bootcamp
            FROM (
                SELECT id_bootcamp, COUNT(id_capacity) AS cap_count
                FROM CAPACITY.BOOTCAMP_CAPACITY
                GROUP BY id_bootcamp
                ORDER BY cap_count %s
            ) AS sub
            LIMIT :limit OFFSET :offset
            """;

    public static final String FETCH_CAPACITIES_BY_BOOTCAMP_IDS_QUERY = """
            SELECT bc.%s, bc.%s
            FROM CAPACITY.BOOTCAMP_CAPACITY bc
            WHERE bc.%s IN (:bootcampIds)
            ORDER BY bc.%s ASC, bc.%s ASC
            """;

}
