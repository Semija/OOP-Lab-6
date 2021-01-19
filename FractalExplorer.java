import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FractalExplorer {


	private int width;
	private int height;

	// Главное окно
	private JFrame frame;

	// Элементы на North
	private JPanel northP;
	private JComboBox chooseF;
	private JLabel textF;

	// Элементы на Center
	private JImageDisplay display;
	private Rectangle2D.Double range;

	// Элементы на South
	private JPanel southP;
	private JButton resetB;
	private JButton saveB;

	// Количество потоков на выполнении
	private int rowsRemaining = 0;

	// Фракталы
	private ArrayList<FractalGenerator> fractals;

	// Текущая директория
	private File nowPath = null;

	// Конструкторы
	public FractalExplorer() {
		this(500);

		// Создание объекта, содержащего диапазон
		this.range = new Rectangle2D.Double();

		// Создание объектов Фракталов
		fractals = new ArrayList<FractalGenerator>();
		fractals.add(new Mandelbrot());
		fractals.add(new Tricorn());
		fractals.add(new BurningShip());
	}

	public FractalExplorer(int size) {
		this(size, size);

		// Создание объекта, содержащего диапазон
		this.range = new Rectangle2D.Double();

		// Создание объектов Фракталов
		fractals = new ArrayList<FractalGenerator>();
		fractals.add(new Mandelbrot());
		fractals.add(new Tricorn());
		fractals.add(new BurningShip());
	}

	public FractalExplorer(int width, int height) {
		this.width = width;
		this.height = height;

		// Создание объекта, содержащего диапазон
		this.range = new Rectangle2D.Double();

		// Создание объектов Фракталов
		fractals = new ArrayList<FractalGenerator>();
		fractals.add(new Mandelbrot());
		fractals.add(new Tricorn());
		fractals.add(new BurningShip());
	}

	//Классы обработки событий нажатия кнопки и мыши
	private class resetButton implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// Сброс границ фрактала и вызов функции отрисовки
			int index = chooseF.getSelectedIndex();
			if (index >= fractals.size()) {
				FractalExplorer.this.clearImage();
				return;
			}
			fractals.get(index).getInitialRange(range);
			FractalExplorer.this.drawFractal(index);
		}
	}

	private class saveButton implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser fchooser;

			// Создание диалогового окна для получения пути сохранения файла
			if (nowPath == null) {
				fchooser = new JFileChooser();
			} else {
				fchooser = new JFileChooser(nowPath);
			}

			// Настройка пути
			fchooser.setDialogTitle("Choose path");

			// Настройка фильтров
			fchooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Images", "*.png"));
			fchooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Images", "*.jpeg"));
			fchooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP Images", "*.bmp"));
			fchooser.setAcceptAllFileFilterUsed(false);


			int result = fchooser.showSaveDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				System.out.println("Directory get");
			} else {
				System.out.println("Directory get ERROR");
				return;
			}

			// Получение полного пути
			String ext = "";
			String extension = fchooser.getFileFilter().getDescription();
			if (extension.equals("PNG Images")) ext = "png";
			if (extension.equals("JPEG Images")) ext = "jpeg";
			if (extension.equals("BMP Images")) ext = "bmp";
			nowPath = new File(fchooser.getSelectedFile().getPath() + "." + ext);

			// Запись файла на диск
			try
			{
				ImageIO.write(display.getImage(), ext, nowPath);
				JOptionPane.showMessageDialog(FractalExplorer.this.frame, "Save is success!", "File save", JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(FractalExplorer.this.frame, "Save is failed!", "File save", JOptionPane.WARNING_MESSAGE);
			}

		}
	}

	private class mouseClick implements MouseListener {
		public void mouseClicked(MouseEvent e) {

			int index = chooseF.getSelectedIndex();
			if (index >= fractals.size()) return;

			// Координаты клика мыши
			int x = e.getX();
			int y = e.getY();

			// Перевод координат в комплексную плоскость
			double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, display.getWidth(), x);
			double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, display.getHeight(), y);

			// Нажатие левой кнопкой мыши
			if (e.getButton() == MouseEvent.BUTTON1) {
				// Приближение
				fractals.get(index).recenterAndZoomRange(range, xCoord, yCoord, 0.5);
			}

			// Нажатие правой кнопкой мыши
			if (e.getButton() == MouseEvent.BUTTON3) {
				// Отдаление
				fractals.get(index).recenterAndZoomRange(range, xCoord, yCoord, 1.5);
			}

			FractalExplorer.this.drawFractal(index);
		}

		/*
		 * Need just to override them (error with russian words here)
		 */
		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}
	}

	private class comboBoxClickListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			int index = chooseF.getSelectedIndex();

			if (index >= fractals.size()) {
				FractalExplorer.this.clearImage();
				return;
			}

			// Настройка начального диапазона фрактала
			fractals.get(index).getInitialRange(range);

			// Вызов функции рисования
			FractalExplorer.this.drawFractal(index);
		}
	}

	// Создание окна с компонентами
	public void createAndShowGUI() {
		// Создание окна
		this.frame = new JFrame("Фракталы");
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.frame.setSize(this.width, this.height);
		this.frame.setResizable(false);

		// Создание панелей
		northP = new JPanel();
		southP = new JPanel();

		// Добавление кнопок
		this.resetB = new JButton("Reset display");
		this.resetB.setPreferredSize(new Dimension(frame.getWidth() / 3, 30));
		southP.add(this.resetB);

		this.saveB = new JButton("Save image");
		this.saveB.setPreferredSize(new Dimension(frame.getWidth() / 3, 30));
		southP.add(this.saveB);

		// Текст сверху
		this.textF = new JLabel("Фракталы: ");
		Font font = saveB.getFont();
		textF.setFont(font);
		northP.add(this.textF);

		// Создание и заполнение списка элементами
		this.chooseF = new JComboBox();
		for (int i = 0; i < fractals.size(); i++) {
			chooseF.addItem(fractals.get(i).toString());
		}

		// Начальный пустой элемент
		chooseF.addItem("Empty");
		chooseF.setSelectedIndex(fractals.size());
		this.chooseF.setPreferredSize(new Dimension(frame.getWidth() / 4, 30));
		northP.add(this.chooseF);

		// Добавление панелей на форму
		frame.getContentPane().add(BorderLayout.NORTH, this.northP);
		frame.getContentPane().add(BorderLayout.SOUTH, this.southP);
		int height = frame.getHeight() - 60;
		int width = height;
		frame.setSize(width, frame.getHeight());

		// Создание панели отрисовки
		this.display = new JImageDisplay(width, height);
		frame.getContentPane().add(BorderLayout.CENTER, this.display);

		// Добавление кнопок
		display.addMouseListener(new mouseClick());
		resetB.addActionListener(new resetButton());
		saveB.addActionListener(new saveButton());
		chooseF.addActionListener(new comboBoxClickListener());

		frame.setVisible(true);
	}

	// Нарисовать фрактал
	public void drawFractal(int index) {
		this.clearImage();

		// запуск многопоточности
		this.enableUI(false);
		int pictureHeight = display.getHeight();
		int pictureWidth = display.getWidth();
		rowsRemaining = pictureHeight;

		for (int i = 0; i < pictureHeight; i++) {
			FractalWorker tempThread = new FractalWorker(i, pictureWidth, index);
			tempThread.execute();
		}

		/*
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, display.getWidth(), x);
				double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, display.getHeight(), y);

				// Определение итерации точки в множество
				int numOfIter = fractals.get(index).numIterations(xCoord, yCoord);

				int rgbColor;
				if (numOfIter != -1) {
					float hue = 0.7f + (float) numOfIter / 200f;
					rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
				}
				else {
					rgbColor = Color.HSBtoRGB(0, 0, 0);
				}

				display.drawPixel(x, y, new Color(rgbColor));

			}
		}
		*/
	}

	public void clearImage() {
		this.display.clearImage();
	}

	/*
	* Многопоточность
	*/
	private class FractalWorker extends SwingWorker<Object, Object> {
		private int numOfStr;
		private int picWidth;
		private int[] strValues;
		
		// Индекс фрактала
		private int index;

		// конструктор
		public FractalWorker(int y, int picWidth, int index) {
			this.numOfStr = y;
			this.picWidth = picWidth;
			this.index = index;
		}

		// Выполнение фонового потока
		@Override
        protected String doInBackground() throws Exception {
			strValues = new int[picWidth];
			
			for (int x = 0; x < picWidth; x++) {
				double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, display.getWidth(), x);
				double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, display.getHeight(), numOfStr);
				strValues[x] = fractals.get(index).numIterations(xCoord, yCoord);
			}
			
			return null;
		}
		
		// Завершение работы фонового потока
		@Override
        protected void done() {
			try {
				int x = 0;
				for (int numOfIter: strValues) {
					int rgbColor;
					if (numOfIter != -1) {
						float hue = 0.7f + (float) numOfIter / 200f; 
						rgbColor = Color.HSBtoRGB(hue, 1f, 1f); 
					} 
					else {
						rgbColor = Color.HSBtoRGB(0, 0, 0); 
					}
					display.drawPixelWithNoRepaint(x, numOfStr, new Color(rgbColor));
					
					x++;
				}
			} 
			catch (Exception e) { 
				e.printStackTrace(); 
            }
			rowsRemaining--;
			if (rowsRemaining == 0) {
				enableUI(true);
				display.repaintPicture();
			}
		} 
	}
	
	// Включение UI
	private void enableUI(boolean enable) {
		chooseF.setEnabled(enable);
		saveB.setEnabled(enable);
		resetB.setEnabled(enable);
	}

	public static void main(String[] args) {
		FractalExplorer explorer = new FractalExplorer(600);
		explorer.createAndShowGUI();
	}
}