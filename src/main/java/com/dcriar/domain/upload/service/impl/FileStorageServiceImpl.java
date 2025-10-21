package com.dcriar.domain.upload.service.impl;

import com.dcriar.domain.upload.service.FileStorageService;
import com.dcriar.exception.custom.ArquivoNaoEncontradoException;
import com.dcriar.exception.custom.ArquivoStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementação do serviço de armazenamento de arquivos que salva os arquivos no sistema de arquivos local.
 * Esta classe é responsável por criar o diretório de upload, armazenar, carregar e excluir arquivos.
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    /**
     * Construtor que inicializa o serviço de armazenamento de arquivos.
     * Ele obtém o diretório de upload a partir da propriedade 'file.upload-dir' definida no application.properties,
     * e cria o diretório se ele não existir.
     *
     * @param uploadDir O caminho para o diretório de upload.
     * @throws ArquivoStorageException Se não for possível criar o diretório de upload.
     */
    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new ArquivoStorageException("Não foi possível criar o diretório onde os arquivos de upload serão armazenados.", ex);
        }
    }

    /**
     * Armazena um arquivo no sistema de arquivos.
     * O nome do arquivo original é limpo e um UUID é prependido para garantir a unicidade e evitar conflitos.
     *
     * @param file O arquivo multipart a ser armazenado.
     * @return O novo nome do arquivo gerado.
     * @throws ArquivoStorageException Se o nome do arquivo contiver sequências de caminho inválidas ou se ocorrer um erro de IO.
     */
    @Override
    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        if (originalFileName.contains("..")) {
            throw new ArquivoStorageException("Desculpe! O nome do arquivo contém uma sequência de caminho inválida: " + originalFileName);
        }

        if (originalFileName.isBlank()) {
            throw new ArquivoStorageException("Desculpe! O nome do arquivo é inválido ou vazio.");
        }

        if (file.isEmpty()) {
            throw new ArquivoStorageException("Desculpe! O arquivo enviado está vazio: " + originalFileName);
        }

        try (InputStream inputStream = file.getInputStream()) {
            String newFileName = UUID.randomUUID() + "_" + originalFileName;

            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;
        } catch (IOException ex) {
            throw new ArquivoStorageException("Não foi possível armazenar o arquivo " + originalFileName + ". Por favor, tente novamente!", ex);
        }
    }

    /**
     * Carrega um arquivo como um recurso a partir do sistema de arquivos.
     *
     * @param fileName O nome do arquivo a ser carregado.
     * @return O arquivo como um objeto Resource.
     * @throws ArquivoNaoEncontradoException Se o arquivo não for encontrado.
     * @throws ArquivoStorageException Se a URL do recurso for malformada.
     */
    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ArquivoNaoEncontradoException("Arquivo não encontrado: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ArquivoStorageException("Erro ao formar a URL para o arquivo: " + fileName, ex);
        }
    }

    /**
     * Exclui um arquivo do sistema de arquivos.
     * Se o nome do arquivo for nulo ou vazio, a operação é ignorada.
     *
     * @param fileName O nome do arquivo a ser excluído.
     * @throws ArquivoStorageException se ocorrer um erro de IO durante a exclusão.
     */
    @Override
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Arquivo de foto antigo excluído com sucesso: {}", fileName);
        } catch (IOException ex) {
            log.error("Não foi possível excluir o arquivo: {}", fileName, ex);
            throw new ArquivoStorageException("Não foi possível excluir o arquivo " + fileName, ex);
        }
    }

    /**
     * Extrai o nome do arquivo de uma URL de download.
     * Útil para obter o nome do arquivo a partir da URL armazenada no banco de dados.
     *
     * @param fileUrl A URL completa do arquivo.
     * @return O nome do arquivo extraído ou nulo se a URL for inválida.
     */
    @Override
    public String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        try {
            return Paths.get(fileUrl).getFileName().toString();
        } catch (Exception e) {
            log.error("Erro ao extrair o nome do arquivo da URL: {}", fileUrl, e);
            return null;
        }
    }
}