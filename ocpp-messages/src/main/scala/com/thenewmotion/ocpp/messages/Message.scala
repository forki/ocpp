package com.thenewmotion.ocpp
package messages

import scala.concurrent.duration._
import java.net.URI
import java.time.ZonedDateTime

sealed trait Message
sealed trait Req extends Message
sealed trait Res extends Message

@SerialVersionUID(0)
sealed trait CentralSystemMessage extends Message
sealed trait CentralSystemReq extends CentralSystemMessage with Req
sealed trait CentralSystemRes extends CentralSystemMessage with Res


case class AuthorizeReq(idTag: IdTag) extends CentralSystemReq
case class AuthorizeRes(idTag: IdTagInfo) extends CentralSystemRes


case class StartTransactionReq(connector: ConnectorScope,
                               idTag: IdTag,
                               timestamp: ZonedDateTime,
                               meterStart: Int,
                               reservationId: Option[Int]) extends CentralSystemReq
case class StartTransactionRes(transactionId: Int, idTag: IdTagInfo) extends CentralSystemRes


case class StopTransactionReq(transactionId: Int,
                              idTag: Option[IdTag],
                              timestamp: ZonedDateTime,
                              meterStop: Int,
                              transactionData: List[TransactionData]) extends CentralSystemReq
case class StopTransactionRes(idTag: Option[IdTagInfo]) extends CentralSystemRes


case object HeartbeatReq extends CentralSystemReq
case class HeartbeatRes(currentTime: ZonedDateTime) extends CentralSystemRes


case class MeterValuesReq(scope: Scope, transactionId: Option[Int], meters: List[Meter]) extends CentralSystemReq
case object MeterValuesRes extends CentralSystemRes


case class BootNotificationReq(chargePointVendor: String,
                               chargePointModel: String,
                               chargePointSerialNumber: Option[String],
                               chargeBoxSerialNumber: Option[String],
                               firmwareVersion: Option[String],
                               iccid: Option[String],
                               imsi: Option[String],
                               meterType: Option[String],
                               meterSerialNumber: Option[String]) extends CentralSystemReq
case class BootNotificationRes(registrationAccepted: Boolean,
                               currentTime: ZonedDateTime /*optional in OCPP 1.2*/ ,
                               heartbeatInterval: FiniteDuration /*optional in OCPP 1.2*/) extends CentralSystemRes

case class CentralSystemDataTransferReq(vendorId: String, messageId: Option[String], data: Option[String])
  extends CentralSystemReq

case class CentralSystemDataTransferRes(status: DataTransferStatus.Value, data: Option[String] = None)
  extends CentralSystemRes

case class StatusNotificationReq(scope: Scope,
                                 status: ChargePointStatus,
                                 timestamp: Option[ZonedDateTime],
                                 vendorId: Option[String]) extends CentralSystemReq
case object StatusNotificationRes extends CentralSystemRes


case class FirmwareStatusNotificationReq(status: FirmwareStatus.Value) extends CentralSystemReq
case object FirmwareStatusNotificationRes extends CentralSystemRes


case class DiagnosticsStatusNotificationReq(uploaded: Boolean) extends CentralSystemReq
case object DiagnosticsStatusNotificationRes extends CentralSystemRes



case class TransactionData(meters: List[Meter])

sealed trait ChargePointStatus {
  def info: Option[String]
}
case class Available(info:Option[String]=None) extends ChargePointStatus
case class Occupied(info:Option[String]=None) extends ChargePointStatus
case class Faulted(errorCode: Option[ChargePointErrorCode.Value],
                   info: Option[String]=None,
                   vendorErrorCode: Option[String]) extends ChargePointStatus
case class Unavailable(info:Option[String]=None) extends ChargePointStatus
// since OCPP 1.5
case class Reserved(info:Option[String]=None) extends ChargePointStatus

object ChargePointErrorCode extends Enumeration {
  val ConnectorLockFailure,
  HighTemperature,
  Mode3Error,
  PowerMeterFailure,
  PowerSwitchFailure,
  ReaderFailure,
  ResetFailure,
  GroundFailure /*since OCPP 1.5*/ ,
  OverCurrentFailure,
  UnderVoltage,
  WeakSignal,
  OtherError = Value
}

object FirmwareStatus extends Enumeration {
  val Downloaded,
  DownloadFailed,
  InstallationFailed,
  Installed = Value
}

@SerialVersionUID(0)
sealed trait ChargePointMessage extends Message
sealed trait ChargePointReq extends ChargePointMessage with Req
sealed trait ChargePointRes extends ChargePointMessage with Res


case class ChargingSchedulePeriod(
  startOffset: FiniteDuration,
  amperesLimit: Double,
  numberPhases: Option[Int]
)

case class ChargingSchedule(
  chargingRateUnit: UnitOfChargingRate,
  chargingSchedulePeriod: List[ChargingSchedulePeriod],
  minChargingRate: Option[Double],
  startsAt: Option[ZonedDateTime],
  duration: Option[FiniteDuration]
)

case class ChargingProfile(
  id: Int,
  stackLevel: Int,
  chargingProfilePurpose: ChargingProfilePurpose,
  chargingProfileKind: ChargingProfileKind,
  chargingSchedule: ChargingSchedule,
  transactionId: Option[Int],
  validFrom: Option[ZonedDateTime],
  validTo: Option[ZonedDateTime]
)

case class SetChargingProfileReq(
  connector: Scope,
  chargingProfile: ChargingProfile
) extends ChargePointReq
case class SetChargingProfileRes(
  status: ChargingProfileStatus
) extends ChargePointRes

case class ClearChargingProfileReq(
  id: Option[Int],
  connector: Option[Scope],
  chargingProfilePurpose: Option[ChargingProfilePurpose],
  stackLevel: Option[Int]
) extends ChargePointReq
case class ClearChargingProfileRes(
  status: ClearChargingProfileStatus
) extends ChargePointRes

case class GetCompositeScheduleReq(
  connector: Scope,
  duration: FiniteDuration,
  chargingRateUnit: Option[UnitOfChargingRate]
) extends ChargePointReq
case class GetCompositeScheduleRes(
  status: GetCompositeScheduleStatus
) extends ChargePointRes

case class RemoteStartTransactionReq(
  idTag: IdTag,
  connector: Option[ConnectorScope],
  chargingProfile: Option[ChargingProfile]
) extends ChargePointReq
case class RemoteStartTransactionRes(accepted: Boolean) extends ChargePointRes


case class RemoteStopTransactionReq(transactionId: Int) extends ChargePointReq
case class RemoteStopTransactionRes(accepted: Boolean) extends ChargePointRes


case class UnlockConnectorReq(connector: ConnectorScope) extends ChargePointReq
case class UnlockConnectorRes(accepted: Boolean) extends ChargePointRes


case class GetDiagnosticsReq(location: URI,
                             startTime: Option[ZonedDateTime],
                             stopTime: Option[ZonedDateTime],
                             retries: Retries) extends ChargePointReq
case class GetDiagnosticsRes(fileName: Option[String]) extends ChargePointRes


case class ChangeConfigurationReq(key: String, value: String) extends ChargePointReq
case class ChangeConfigurationRes(status: ConfigurationStatus.Value) extends ChargePointRes


case class GetConfigurationReq(keys: List[String]) extends ChargePointReq
case class GetConfigurationRes(values: List[KeyValue], unknownKeys: List[String]) extends ChargePointRes


case class ChangeAvailabilityReq(scope: Scope, availabilityType: AvailabilityType.Value) extends ChargePointReq
case class ChangeAvailabilityRes(status: AvailabilityStatus.Value) extends ChargePointRes


case object ClearCacheReq extends ChargePointReq
case class ClearCacheRes(accepted: Boolean) extends ChargePointRes


case class ResetReq(resetType: ResetType.Value) extends ChargePointReq
case class ResetRes(accepted: Boolean) extends ChargePointRes


case class UpdateFirmwareReq(retrieveDate: ZonedDateTime, location: URI, retries: Retries) extends ChargePointReq
case object UpdateFirmwareRes extends ChargePointRes


case class SendLocalListReq(updateType: UpdateType.Value,
                            listVersion: AuthListSupported,
                            localAuthorisationList: List[AuthorisationData],
                            hash: Option[String]) extends ChargePointReq

case class SendLocalListRes(status: UpdateStatus.Value) extends ChargePointRes


case object GetLocalListVersionReq extends ChargePointReq
case class GetLocalListVersionRes(version: AuthListVersion) extends ChargePointRes


case class ChargePointDataTransferReq(vendorId: String, messageId: Option[String], data: Option[String])
  extends ChargePointReq

case class ChargePointDataTransferRes(status: DataTransferStatus.Value, data: Option[String] = None)
  extends ChargePointRes


case class ReserveNowReq(connector: Scope,
                         expiryDate: ZonedDateTime,
                         idTag: IdTag,
                         parentIdTag: Option[String] = None,
                         reservationId: Int) extends ChargePointReq
case class ReserveNowRes(status: Reservation.Value) extends ChargePointRes


case class CancelReservationReq(reservationId: Int) extends ChargePointReq
case class CancelReservationRes(accepted: Boolean) extends ChargePointRes



object ConfigurationStatus extends Enumeration {
  val Accepted, Rejected, NotSupported = Value
}

object AvailabilityStatus extends Enumeration {
  val Accepted, Rejected, Scheduled = Value
}

object AvailabilityType extends Enumeration {
  val Operative, Inoperative = Value
}

object ResetType extends Enumeration {
  val Hard, Soft = Value
}

case class KeyValue(key: String, readonly: Boolean, value: Option[String])

object UpdateType extends Enumeration {
  val Differential, Full = Value
}

object UpdateStatus {
  sealed trait Value
  case class UpdateAccepted(hash: Option[String]) extends Value
  case object UpdateFailed extends Value
  case object HashError extends Value
  case object NotSupportedValue extends Value
  case object VersionMismatch extends Value
}

sealed trait AuthorisationData {
  def idTag: IdTag
}

object AuthorisationData {
  def apply(idTag: IdTag, idTagInfo: Option[IdTagInfo]): AuthorisationData = idTagInfo match {
    case Some(x) => AuthorisationAdd(idTag, x)
    case None => AuthorisationRemove(idTag)
  }
}

case class AuthorisationAdd(idTag: IdTag, idTagInfo: IdTagInfo) extends AuthorisationData
case class AuthorisationRemove(idTag: IdTag) extends AuthorisationData


object Reservation extends Enumeration {
  val Accepted, Faulted, Occupied, Rejected, Unavailable = Value
}

object AuthListVersion {
  def apply(version: Int): AuthListVersion =
    if (version < 0) AuthListNotSupported else AuthListSupported(version)
}
sealed trait AuthListVersion
case object AuthListNotSupported extends AuthListVersion
case class AuthListSupported(version: Int) extends AuthListVersion {
  require(version >= 0, s"version which is $version must be greater than or equal to 0")
}

case class Retries(numberOfRetries: Option[Int], interval: Option[FiniteDuration]) {
  def intervalInSeconds: Option[Int] = interval.map(_.toSeconds.toInt)
}

object Retries {
  val none = Retries(None, None)

  def fromInts(numberOfRetries: Option[Int], intervalInSeconds: Option[Int]): Retries =
    Retries(numberOfRetries, intervalInSeconds.map(_.seconds))
}
