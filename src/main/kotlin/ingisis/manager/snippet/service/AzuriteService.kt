package ingisis.manager.snippet.service

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Service
class AzuriteService {
    @Value("\${azure.storage.connection-string}")
    private lateinit var connectionString: String

    @Value("\${azure.storage.container-name}")
    private lateinit var containerName: String

    private fun getContainerClient(): BlobContainerClient {
        val blobServiceClient: BlobServiceClient = BlobServiceClientBuilder().connectionString(connectionString).buildClient()
        return blobServiceClient.getBlobContainerClient(containerName)
    }

    fun getSnippetContent(snippetId: String): InputStream? {
        val containerClient = getContainerClient()
        val blobClient: BlobClient = containerClient.getBlobClient(snippetId)

        return if (blobClient.exists()) {
            val outputStream = ByteArrayOutputStream()
            blobClient.download(outputStream)
            ByteArrayInputStream(outputStream.toByteArray())
        } else {
            null
        }
    }

    fun uploadContentToAzurite(
        snippetId: String,
        content: String,
    ): String {
        val containerClient = getContainerClient()
        val blobClient: BlobClient = containerClient.getBlobClient(snippetId)

        val contentStream: InputStream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))

        blobClient.upload(contentStream, content.length.toLong(), true)

        println("Snippet content '$snippetId' uploaded successfully.")
        return blobClient.blobUrl.toString()
    }
}
