package com.pedropathingplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class PedroPathReaderTest {

    @Test
    public void testGetAvailablePathNames() throws IOException {
        // Mock Context and AssetManager
        Context mockContext = mock(Context.class);
        AssetManager mockAssetManager = mock(AssetManager.class);

        // Configure mocks
        when(mockContext.getAssets()).thenReturn(mockAssetManager);

        // Setup expected files in "AutoPaths"
        String[] files = {"path1.json", "path2.json", "readme.txt", "subfolder"};
        when(mockAssetManager.list("AutoPaths")).thenReturn(files);

        // Call the method under test
        List<String> availablePaths = PedroPathReader.getAvailablePathNames(mockContext);

        // Verify results
        assertEquals("Should find 2 json files", 2, availablePaths.size());
        assertTrue("Should contain path1.json", availablePaths.contains("path1.json"));
        assertTrue("Should contain path2.json", availablePaths.contains("path2.json"));
        assertTrue("Should not contain readme.txt", !availablePaths.contains("readme.txt"));
    }

    @Test
    public void testGetAvailablePathNames_Empty() throws IOException {
        Context mockContext = mock(Context.class);
        AssetManager mockAssetManager = mock(AssetManager.class);
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.list("AutoPaths")).thenReturn(new String[0]);

        List<String> availablePaths = PedroPathReader.getAvailablePathNames(mockContext);

        assertTrue("Should be empty", availablePaths.isEmpty());
    }

    @Test
    public void testGetAvailablePathNames_Exception() throws IOException {
        Context mockContext = mock(Context.class);
        AssetManager mockAssetManager = mock(AssetManager.class);
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.list("AutoPaths")).thenThrow(new IOException("Asset error"));

        List<String> availablePaths = PedroPathReader.getAvailablePathNames(mockContext);

        assertTrue("Should handle exception and return empty list", availablePaths.isEmpty());
    }
}
