package com.example.pozterminal3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.RemoteException
import androidx.annotation.Nullable
import ru.evotor.devices.commons.printer.printable.PrintableBarcode
import ru.evotor.devices.commons.printer.printable.PrintableImage
import ru.evotor.devices.commons.printer.printable.PrintableText
import ru.evotor.framework.core.IntegrationService
import ru.evotor.framework.core.action.event.receipt.changes.receipt.print_extra.SetPrintExtra
import ru.evotor.framework.core.action.event.receipt.print_extra.PrintExtraRequiredEvent
import ru.evotor.framework.core.action.event.receipt.print_extra.PrintExtraRequiredEventProcessor
import ru.evotor.framework.core.action.event.receipt.print_extra.PrintExtraRequiredEventResult
import ru.evotor.framework.core.action.processor.ActionProcessor
import ru.evotor.framework.receipt.ExtraKey
import ru.evotor.framework.receipt.Receipt
import ru.evotor.framework.receipt.ReceiptApi.getReceipt
import ru.evotor.framework.receipt.print_extras.*
import java.io.IOException
import java.io.InputStream
import java.util.*
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.ReceiptApi
import ru.evotor.framework.receipt.print_extras.PrintExtraPlacePositionAllSubpositionsFooter
import ru.evotor.framework.receipt.print_extras.PrintExtraPlacePositionFooter
import ru.evotor.framework.receipt.print_extras.PrintExtraPlacePrintGroupHeader
import ru.evotor.framework.receipt.print_extras.PrintExtraPlacePrintGroupSummary
import ru.evotor.framework.receipt.print_extras.PrintExtraPlacePrintGroupTop
//import java.io.IOException
//import java.io.InputStream
//import java.util.*


/**
 * Печать внутри кассового чека продажи
 * В манифесте добавить права <uses-permission android:name="ru.evotor.permission.receipt.printExtra.SET"></uses-permission>
 * В манифесте для сервиса указать:
 * - печать внутри кассового чека продажи <action android:name="evo.v2.receipt.sell.printExtra.REQUIRED"></action>
 * Штрихкод должен быть с контрольной суммой
 */
class MyPrintableService : IntegrationService() {
    /**
     * Получение картинки из каталога asset приложения
     *
     * @param fileName имя файла
     * @return значение типа Bitmap
     */
    private fun getBitmapFromAsset(fileName: String): Bitmap {
        val assetManager = assets
        var stream: InputStream? = null
        try {
            stream = assetManager.open(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return BitmapFactory.decodeStream(stream)
    }

    @Nullable
    override fun createProcessors(): Map<String, ActionProcessor>? {
        val map: MutableMap<String, ActionProcessor> =
            HashMap()
        map[PrintExtraRequiredEvent.NAME_SELL_RECEIPT] =
            object : PrintExtraRequiredEventProcessor() {
                override fun call(
                    s: String,
                    printExtraRequiredEvent: PrintExtraRequiredEvent,
                    callback: Callback
                ) {
                    val setPrintExtras: MutableList<SetPrintExtra> =
                        ArrayList()
                    setPrintExtras.add(
                        SetPrintExtra( //Метод, который указывает место, где будут распечатаны данные.
                            //Данные печатаются после клише и до текста “Кассовый чек”
                            PrintExtraPlacePrintGroupTop(null), arrayOf( //Простой текст
                                PrintableText("Получилось!")  //Штрихкод с контрольной суммой если она требуется для выбранного типа штрихкода
                            )
                        )
                    )
                    try {
                        callback.onResult(PrintExtraRequiredEventResult(setPrintExtras).toBundle())
                    } catch (exc: RemoteException) {
                        exc.printStackTrace()
                    }
                }
            }
        return map
        stopSelf()
    }

}