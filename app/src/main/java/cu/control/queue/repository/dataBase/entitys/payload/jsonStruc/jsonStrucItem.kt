package cu.control.queue.repository.dataBase.entitys.payload.jsonStruc

data class jsonStrucItem(
    val geo_data: GeoData,
    val id: String,
    val municipality: List<Municipality>,
    val name: String
)