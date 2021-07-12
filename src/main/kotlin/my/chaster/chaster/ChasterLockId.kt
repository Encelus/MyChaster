package my.chaster.chaster

import my.chaster.gen.chaster.model.LockForKeyholder
import my.chaster.gen.chaster.model.LockForPublic
import javax.persistence.AttributeConverter
import javax.persistence.Converter

data class ChasterLockId(val id: String)

@Converter(autoApply = true)
class ChasterLockIdConverter : AttributeConverter<ChasterLockId?, String?> {

	override fun convertToDatabaseColumn(attribute: ChasterLockId?): String? {
		return attribute?.id
	}

	override fun convertToEntityAttribute(dbData: String?): ChasterLockId? {
		return dbData?.let { ChasterLockId(it) }
	}

}

fun LockForPublic.getChasterId(): ChasterLockId {
	return ChasterLockId(id)
}

fun LockForKeyholder.getChasterId(): ChasterLockId {
	return ChasterLockId(id)
}