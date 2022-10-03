package com.vtitan.uroflowmetry.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.UroTestModel
import java.io.Serializable

data class UroTestWithUroInfo (
    @Embedded val uroTestModel: UroTestModel,
    @Relation(
        parentColumn = "test_id",
        entityColumn = "test_id"
    )
    val uroInfoModel:List<UroInfoModel>
) : Serializable