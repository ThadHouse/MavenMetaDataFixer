package edu.wpi.first.maven

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.XmlUtil

import java.security.MessageDigest
import java.text.SimpleDateFormat


@CompileStatic
class MetaDataFixer {
    private String pathRoot

    MetaDataFixer(String pathRoot) {
        this.pathRoot = pathRoot
    }

    void updateMetaData() {
        recureMetaData(new File(pathRoot))
    }

    private void fixHashes(File dataFile, String text) {
        def txtBytes = text.bytes
        def md5 = MessageDigest.getInstance("MD5").digest(txtBytes).encodeHex().toString()
        def md5File = new File(dataFile.toString() + '.md5')
        md5File.text = md5
        def sha1 = MessageDigest.getInstance("SHA1").digest(txtBytes).encodeHex().toString()
        def sha1File = new File(dataFile.toString() + '.sha1')
        sha1File.text = sha1
    }

    private List<String> getVersionsForMetaData(File dataFile, String artifactId) {
        def parent = dataFile.parentFile

        def versions = []

        parent.eachDir { dir ->
            def dirName = dir.name
            def pomFile = new File(dir, "$artifactId-${dirName}.pom")
            if (pomFile.exists()) {
                versions << dirName
            }

        }

        return versions
    }

    @CompileDynamic
    private void updateSpecificMetaData(File dataFile) {
        def parser = new XmlParser()
        def parsed = parser.parseText(dataFile.text)

        // Get versions we should have
        String artifactId = parsed.artifactId.text()
        def versions = getVersionsForMetaData(dataFile, artifactId)

        // Remove all existing versions from metadata file
        parsed.versioning.versions.version.each {
            it.replaceNode {
                // Replace with empty node
            }
        }
        versions.sort().reverse().each {
            new Node(parsed.versioning.versions[0] as Node, 'version', null, it)
        }

        final updateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
        final lu = updateFormat.format(new Date())
        parsed.versioning.lastUpdated.replaceNode {
            lastUpdated(lu)
        }
        def serialized = XmlUtil.serialize(parsed)
        dataFile.text = serialized
        fixHashes(dataFile, serialized)
    }

    private void recureMetaData(File root) {
        File metaDataFile = new File(root, "maven-metadata.xml")
        if (metaDataFile.isFile()) {
            updateSpecificMetaData(metaDataFile)
        }
        root.eachDir { dir ->
            recureMetaData(dir)
        }
    }
}
