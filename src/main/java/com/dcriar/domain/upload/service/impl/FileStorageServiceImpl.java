package com.dcriar.domain.upload.service.impl;

import com.dcriar.domain.upload.service.FileStorageService;
import com.dcriar.exception.custom.FileNotFoundException;
import com.dcriar.exception.custom.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     * @throws FileStorageException Se não for possível criar o diretório de upload.
     */
    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Não foi possível criar o diretório onde os arquivos de upload serão armazenados.", ex);
        }
    }

    /**
     * Armazena um arquivo no sistema de arquivos.
     * O nome do arquivo original é limpo e um UUID é prependido para garantir a unicidade e evitar conflitos.
     *
     * @param file O arquivo multipart a ser armazenado.
     * @return O novo nome do arquivo gerado.
     * @throws FileStorageException Se o nome do arquivo contiver sequências de caminho inválidas ou se ocorrer um erro de IO.
     */
    @Override
    public String storeFile(MultipartFile file) {
        // Limpa e normaliza o nome do arquivo original para remover quaisquer caracteres ou sequências de caminho perigosas.
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // Validação de segurança para evitar ataques de "path traversal".
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Desculpe! O nome do arquivo contém uma sequência de caminho inválida: " + originalFileName);
            }

            // Gera um nome de arquivo único para evitar sobrescrever arquivos existentes.
            String newFileName = UUID.randomUUID() + "_" + originalFileName;

            // Resolve o caminho completo onde o arquivo será salvo.
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            // Copia o conteúdo do arquivo para o local de destino. Se um arquivo com o mesmo nome já existir, ele será substituído.
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possível armazenar o arquivo " + originalFileName + ". Por favor, tente novamente!", ex);
        }
    }

    /**
     * Carrega um arquivo como um recurso a partir do sistema de arquivos.
     *
     * @param fileName O nome do arquivo a ser carregado.
     * @return O arquivo como um objeto Resource.
     * @throws FileNotFoundException Se o arquivo não for encontrado ou a URL do recurso for malformada.
     */
    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("Arquivo não encontrado: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("Arquivo não encontrado: " + fileName, ex);
        }
    }

    /**
     * Exclui um arquivo do sistema de arquivos.
     * Se o nome do arquivo for nulo ou vazio, a operação é ignorada.
     *
     * @param fileName O nome do arquivo a ser excluído.
     */
    @Override
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Arquivo excluído com sucesso: {}", fileName);
        } catch (IOException ex) {
            log.error("Não foi possível excluir o arquivo: {}", fileName, ex);
            // Dependendo do caso de uso, você pode querer lançar uma exceção aqui
            // Por enquanto, apenas registramos o erro e continuamos
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
