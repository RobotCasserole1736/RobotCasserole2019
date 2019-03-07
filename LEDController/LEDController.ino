#include <FastLED.h>

#define DATA_PIN    6
//#define CLK_PIN   4
#define LED_TYPE    WS2811
#define COLOR_ORDER RGB
#define NUM_LEDS    30
CRGB leds[NUM_LEDS];

#define BRIGHTNESS          96
#define FRAMES_PER_SECOND  120

int rioControlPin = 4;
int PWMV;
int patternNumber = 0;
String patternName = "???";
int currentCount;

void setup() {
  Serial.begin(57600);
  Serial.println("Init Start");
  delay(3000); // 3 second delay for recovery

  // tell FastLED about the LED strip configuration
  FastLED.addLeds<LED_TYPE, DATA_PIN, COLOR_ORDER>(leds, NUM_LEDS).setCorrection(TypicalLEDStrip);
  
  // set master brightness control
  FastLED.setBrightness(BRIGHTNESS);


  pinMode(rioControlPin, INPUT);

  Serial.println("Init Complete");
}

uint8_t gHue = 0; // rotating "base color" used by many of the patterns

void readPWM() {
  PWMV = pulseIn(rioControlPin, HIGH, 50000);
}

void loop()
{
  if ((PWMV >= -50) && (PWMV <= 50))
  {
    EVERY_N_MILLISECONDS(50){colorWipe();}
  }

  if ((PWMV >= 950) && (PWMV <= 1050))
  {
    RunningLights(0xff, 0x00, 0x00);
  }

  if ((PWMV >= 1200) && (PWMV <= 1300))
  {
    RunningLights(0xff, 0xff, 0xff);
  }

  if ((PWMV >= 1450) && (PWMV <= 1550))
  {
    Strobe(0xff, 0x00, 0x00);
  }

  if ((PWMV >= 1700) && (PWMV <= 1800))
  {
    meteorRain(0xff, 0xff, 0xff, 10, 64, true, 30);
  }

  if ((PWMV >= 1950) && (PWMV <= 2050))
  {
    CylonBounce(0xff, 0xff, 0xff, 5, 10, 50);
  }

  // send the 'leds' array out to the actual LED strip
  FastLED.show();
  // insert a delay to keep the framerate modest
  FastLED.delay(1000 / FRAMES_PER_SECOND);

  // do some periodic updates
  EVERY_N_MILLISECONDS( 20 ) {
    gHue++;  // slowly cycle the "base color" through the rainbow
  }
  EVERY_N_MILLISECONDS(200) {
    readPWM();  //Ensure we keep PWM reading up to date
  }
}

#define ARRAY_SIZE(A) (sizeof(A) / sizeof((A)[0]))

//**************************************************************
// Pattern: Color Wipe
//**************************************************************

boolean colorWipeIsRed = false;
unsigned int colorWipePixelCounter = 0;

void colorWipe() {
  Serial.println("Running Color Wipe");
  if(colorWipeIsRed) {
    setPixel(colorWipePixelCounter, 0xff, 0x00, 0x00);
  } else {
    setPixel(colorWipePixelCounter, 0xff, 0xff, 0xff);
  }

  colorWipePixelCounter++;
  if(colorWipePixelCounter >= NUM_LEDS){
    colorWipePixelCounter = 0;
    colorWipeIsRed = !colorWipeIsRed;
  }
  
}

//**************************************************************
// Pattern: Cylon
//**************************************************************

void CylonBounce(byte red, byte green, byte blue, int EyeSize, int SpeedDelay, int ReturnDelay) {

  Serial.println("Running Cylon Bounce");
  for (int i = 0; i < NUM_LEDS - EyeSize - 2; i++) {
    setAll(0, 0, 0);
    
    setPixel(i, red / 10, green / 10, blue / 10);
    for (int j = 1; j <= EyeSize; j++) {
      setPixel(i + j, red, green, blue);
    }
    setPixel(i + EyeSize + 1, red / 10, green / 10, blue / 10);
    showStrip();
    delay(SpeedDelay);
  }

  delay(ReturnDelay);

  for (int i = NUM_LEDS - EyeSize - 2; i > 0; i--) {
    setAll(0, 0, 0);
    
    setPixel(i, red / 10, green / 10, blue / 10);
    for (int j = 1; j <= EyeSize; j++) {
      setPixel(i + j, red, green, blue);
    }
    setPixel(i + EyeSize + 1, red / 10, green / 10, blue / 10);
    showStrip();
    delay(SpeedDelay);
  }

  delay(ReturnDelay);
}

//**************************************************************
// Pattern: Meteor Rain
//**************************************************************

void meteorRain(byte red, byte green, byte blue, byte meteorSize, byte meteorTrailDecay, boolean meteorRandomDecay, int SpeedDelay) {
  Serial.println("Running Meteor");
  setAll(0, 0, 0);

  for (int i = 0; i < NUM_LEDS + NUM_LEDS; i++) {
    // fade brightness all LEDs one step
    for (int j = 0; j < NUM_LEDS; j++) {
      if ( (!meteorRandomDecay) || (random(10) > 5) ) {
        fadeToBlack(j, meteorTrailDecay );
      }
    }
    // draw meteor
    for (int j = 0; j < meteorSize; j++) {
      
      if ( ( i - j < NUM_LEDS) && (i - j >= 0) ) {
        setPixel(i - j, red, green, blue);
      }
      
      if( ( j == (meteorSize - 1) ) && ( i - j < NUM_LEDS) && (i - j >= 0) ) {
        if(random(10) > 5){
          Serial.print("setting LED ");
          Serial.println(i-j);
          setPixel(i - j, 0xff, 0x00, 0x00);
        }
      }
      
    }
    showStrip();
    delay(SpeedDelay);
  }
}

void fadeToBlack(int ledNo, byte fadeValue) {
  leds[ledNo].fadeToBlackBy( fadeValue );
}

//**************************************************************
// Pattern: Running Lights
//**************************************************************
int Position = 0;
void RunningLights(byte red, byte green, byte blue) {
  Serial.println("Running Running Lights Wipe");

  Position++; // = 0; //Position + Rate;
  for (int i = 0; i < NUM_LEDS; i++) {
    setPixel(i, ((sin(i + Position / 2) * 127 + 128) / 255)*red,
                ((sin(i + Position / 2) * 127 + 128) / 255)*green,
                ((sin(i + Position / 2) * 127 + 128) / 255)*blue);
  }
}

//**************************************************************
// Pattern: Strobe
//**************************************************************
int strobeCounter = 0;
boolean strobeFlashState;
void Strobe(byte red, byte green, byte blue) {
  Serial.println("Running Strobe");
  strobeCounter = strobeCounter + 1;

  if(strobeCounter > 5){
    strobeCounter = 0;
    strobeFlashState = !strobeFlashState;
  }

  if(strobeFlashState){
    setAll(red, green, blue);
  } else {
    setAll(0, 0, 0); 
  }
}

//**************************************************************
// Strip/Pixel Commands
//**************************************************************

void showStrip() {
  FastLED.show();
}

void setPixel(int Pixel, byte red, byte green, byte blue) {
  // FastLED
  leds[Pixel].g = red;
  leds[Pixel].r = green;
  leds[Pixel].b = blue;
}

void setAll(byte red, byte green, byte blue) {
  for (int i = 0; i < NUM_LEDS; i++ ) {
    setPixel(i, red, green, blue);
  }
  showStrip();
}
