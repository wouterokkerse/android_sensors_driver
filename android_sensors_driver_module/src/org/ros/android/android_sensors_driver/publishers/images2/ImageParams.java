package org.ros.android.android_sensors_driver.publishers.images2;

public class ImageParams {
    public enum ViewMode {
        RGBA,
        GRAY,
        CANNY,
        JPGEG_PICTURES;
    }

    public enum TransportType {
        NONE,
        PNG,
        JPEG;
    }

    public enum CompressionLevel {
        NONE(100),
        VERY_LOW_92(92),
        LOW_80(80),
        MEDIUM(50),
        HIGH(25),
        VERY_HIGH(10);

        private int level;

        private CompressionLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}