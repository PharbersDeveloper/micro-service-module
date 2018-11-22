package services

import com.pharbers.jsonapi.model

object RootObject2DownloadStream {
    implicit class impl(obj: model.RootObject) {
        def toDownloadStream: Array[Byte] =
            obj.data.get.asInstanceOf[model.RootObject.ResourceObject]
                    .attributes.get.head.value
                    .asInstanceOf[model.JsonApiObject.StringValue]
                    .value.getBytes()
    }
}
