package ingisis.manager.snippet.service

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    private val logger: Logger = LoggerFactory.getLogger(AzuriteService::class.java)

    private fun getBlobServiceClient(): BlobServiceClient = BlobServiceClientBuilder().connectionString(connectionString).buildClient()

    // Check if the container exists, if not, create a new one
    private fun getContainerClient(): BlobContainerClient {
        logger.info("Getting container client for container '$containerName'")
        val blobServiceClient = getBlobServiceClient()

        val containerClient: BlobContainerClient = blobServiceClient.getBlobContainerClient(containerName)

        if (!containerClient.exists()) {
            println("Container '$containerName' does not exist. Creating a new one.")
            blobServiceClient.createBlobContainer(containerName)
        }

        return blobServiceClient.getBlobContainerClient(containerName)
    }

    fun getSnippetContent(snippetUrl: String): InputStream? {
        logger.info("Downloading snippet content from Azurite. SnippetUrl: $snippetUrl")

        // Parse the URL to extract the container name and snippetId
        val snippetId = snippetUrl.substringAfterLast("/") // Extract the snippetId from the URL
        logger.info("SnippetId: $snippetId")
        val containerClient = getContainerClient()
        val blobClient: BlobClient = containerClient.getBlobClient(snippetId)

        return if (blobClient.exists()) {
            val outputStream = ByteArrayOutputStream()
            blobClient.download(outputStream)
            logger.info("Snippet content '$snippetId' downloaded successfully.")
            ByteArrayInputStream(outputStream.toByteArray())
        } else {
            logger.info("Blob with snippetId '$snippetId' does not exist. Nothing to download.")
            null
        }
    }

    fun uploadContentToAzurite(
        snippetId: String,
        content: String,
    ): String {
        logger.info("Uploading snippet content to Azurite. SnippetId: $snippetId")
        val containerClient = getContainerClient()
        val blobClient: BlobClient = containerClient.getBlobClient(snippetId)

        val contentStream: InputStream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))

        blobClient.upload(contentStream, content.length.toLong(), true)

        logger.info("Snippet content '$snippetId' uploaded successfully.")
        return blobClient.blobUrl.toString()
    }

    fun deleteContentFromAzurite(snippetId: String) {
        logger.info("Deleting snippet content from Azurite. SnippetId: $snippetId")

        val containerClient = getContainerClient()
        val blobClient: BlobClient = containerClient.getBlobClient(snippetId)

        if (blobClient.exists()) {
            blobClient.delete()
            logger.info("Snippet content '$snippetId' deleted successfully.")
        } else {
            logger.info("Blob with snippetId '$snippetId' does not exist. Nothing to delete.")
        }
    }
}
