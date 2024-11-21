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

    private fun getBlobServiceClient(): BlobServiceClient = BlobServiceClientBuilder().connectionString(connectionString).buildClient()

    // Check if the container exists, if not, create a new one
    private fun getContainerClient(): BlobContainerClient {
        val blobServiceClient = getBlobServiceClient()

        val containerClient: BlobContainerClient = blobServiceClient.getBlobContainerClient(containerName)

        if (!containerClient.exists()) {
            println("Container '$containerName' does not exist. Creating a new one.")
            blobServiceClient.createBlobContainer(containerName)
        }

        return blobServiceClient.getBlobContainerClient(containerName)
    }

    fun getSnippetContent(snippetUrl: String): InputStream? {
        println("Getting snippet content from Azurite. SnippetUrl: $snippetUrl")

        // Parse the URL to extract the container name and snippetId
        val snippetId = snippetUrl.substringAfterLast("/") // Extract the snippetId from the URL
        println("SnippetId: $snippetId")
        val containerClient = getContainerClient()
        val blobClient: BlobClient = containerClient.getBlobClient(snippetId)

        return if (blobClient.exists()) {
            val outputStream = ByteArrayOutputStream()
            blobClient.download(outputStream)
            println("Snippet content '$snippetId' downloaded successfully.")
            ByteArrayInputStream(outputStream.toByteArray())
        } else {
            println("Blob with snippetId '$snippetId' does not exist.")
            null
        }
    }

    fun uploadContentToAzurite(
        snippetId: String,
        content: String,
    ): String {
        println("Upload snippet content to Azurite. SnippetId: $snippetId")
        val containerClient = getContainerClient()
        val blobClient: BlobClient = containerClient.getBlobClient(snippetId)

        val contentStream: InputStream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))

        blobClient.upload(contentStream, content.length.toLong(), true)

        println("Snippet content '$snippetId' uploaded successfully.")
        return blobClient.blobUrl.toString()
    }

    fun deleteContentFromAzurite(snippetId: String) {
        println("Deleting snippet content from Azurite. SnippetId: $snippetId")

        val containerClient = getContainerClient()
        val blobClient: BlobClient = containerClient.getBlobClient(snippetId)

        if (blobClient.exists()) {
            blobClient.delete()
            println("Snippet content '$snippetId' deleted successfully.")
        } else {
            println("Blob with snippetId '$snippetId' does not exist. Nothing to delete.")
        }
    }
}