package zip;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.*;

public class Zip extends JFrame
{
    public Zip()
    {
        this.setTitle("Zipper");
        this.setBounds(275, 300, 250, 250);
        this.setJMenuBar(pasekMenu);
        
        JMenu menuPlik = pasekMenu.add(new JMenu("Plik"));
        
        Action akcjaDodawania = new Akcja ("Dodaj", "Dodaj nowy wpis", "ctrl D");
        Action akcjaUsuwania = new Akcja ("Usuń", "Usuń wpis", "ctrl U");
        Action akcjaZipowania = new Akcja ("Zip", "Archiwizuj", "ctrl Z");
        
        JMenuItem menuOtworz = menuPlik.add(akcjaDodawania);
        JMenuItem menuUsun = menuPlik.add(akcjaUsuwania);
        JMenuItem menuZip = menuPlik.add(akcjaZipowania);
        
        
        bDodaj = new JButton (akcjaDodawania);
        bUsun = new JButton (akcjaUsuwania);
        bZip = new JButton (akcjaZipowania);
        JScrollPane scroll = new JScrollPane(lista);
        
        
        lista.setBorder(BorderFactory.createEtchedBorder()); // ustawiamy wygląd ramki
        GroupLayout layout = new GroupLayout(this.getContentPane());
        
        layout.setAutoCreateContainerGaps(true); //odstępy od ramki
        layout.setAutoCreateGaps(true); // odstępy od buttonów
        
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addComponent(scroll, 100, 150, Short.MAX_VALUE)
            .addContainerGap(0, Short.MAX_VALUE)
            .addGroup(
            layout.createParallelGroup().addComponent(bDodaj).addComponent(bUsun).addComponent(bZip)
            )
        );
        
        layout.setVerticalGroup(
                layout.createParallelGroup()
                .addComponent(scroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup().addComponent(bDodaj).addComponent(bUsun).addGap(5,40, Short.MAX_VALUE).addComponent(bZip))
        );
        
        
        this.getContentPane().setLayout(layout);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        this.pack();
    }
   private DefaultListModel modelListy = new DefaultListModel()
   {
       @Override
       public void addElement(Object obj)
       {
           lista.add(obj);
           super.addElement(((File)obj).getName());
       }
       @Override
       public Object get(int index)
       {
           return lista.get(index);
       }
       @Override
       public Object remove (int index)
       {
           lista.remove(index);
           return super.remove(index);
       }
       
       ArrayList lista = new ArrayList();
   };
   private JList lista = new JList (modelListy);
   private JButton bDodaj;
   private JButton bUsun;
   private JButton bZip;
   private JMenuBar pasekMenu = new JMenuBar();
   private JFileChooser wybieracz = new JFileChooser();
   
    
    
    public static void main(String[] args) 
    {
        new Zip().setVisible(true);
    }
    
    private class Akcja extends AbstractAction
    {
        public Akcja (String nazwa, String opis, String klawiaturowySkrot)
        {
            this.putValue(Action.NAME, nazwa);
            this.putValue(Action.SHORT_DESCRIPTION, opis);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(klawiaturowySkrot));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("Dodaj"))
                dodajWpisDoArchiwum();
            else if (e.getActionCommand().equals("Usuń"))
                usuwanieWpisowZListy ();
            else if(e.getActionCommand().equals("Zip"))
                stworzArchiwumZip();
        }
        
        private void dodajWpisDoArchiwum()
        {
            wybieracz.setCurrentDirectory(new File(System.getProperty("user.dir")));
            wybieracz.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // możliwość wyboru plików i folderów
            wybieracz.setMultiSelectionEnabled(true);  // możliwość zaznaczania kilku plików naraz
            
            int tmp = wybieracz.showDialog(rootPane, "Dodaj do archiwum");
            
            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                File [] sciezki = wybieracz.getSelectedFiles();
                
                for (int i = 0; i < sciezki.length; i++)
                    if (!czyWpisSiePowtarza(sciezki[i].getPath()))
                    modelListy.addElement(sciezki [i]);
            }
            
        }
        
        private boolean czyWpisSiePowtarza (String wpis)
        {
            for (int i = 0; i < modelListy.getSize(); i++)
                if (((File)modelListy.get(i)).getPath().equals(wpis))
                        return true;
            
                        return false;
        }
        private void usuwanieWpisowZListy ()
        {
            int [] tmp = lista.getSelectedIndices();
            
            for (int i = 0; i< tmp.length; i++)
            {
                modelListy.remove(tmp [i]-i);
            System.out.println(tmp[i]);
          
            }     
        }
        private void stworzArchiwumZip()
        {
            wybieracz.setCurrentDirectory(new File(System.getProperty("user.dir")));
            wybieracz.setSelectedFile(new File(System.getProperty("user.dir") + File.separator + "nazwa.zip"));
            int tmp = wybieracz.showDialog(rootPane, "Kompresuj");
            
      
            if (tmp == JFileChooser.APPROVE_OPTION) 
            {
               byte tmpData [] = new byte [BUFFOR];
               
               try
               {
               ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(wybieracz.getSelectedFile()),BUFFOR) );
               
                for (int i = 0; i<modelListy.getSize(); i++)
                    {
                        if(!((File)modelListy.get(i)).isDirectory())
                            zipuj(zOutS, (File)modelListy.get(i), tmpData, ((File)modelListy.get(i)).getPath());
                        else
                        {
                            wypiszSciezki((File)modelListy.get(i));
                            
                            for(int j = 0; j<listaSciezek.size(); j++)
                            {
                                zipuj(zOutS, (File)listaSciezek.get(j), tmpData, ((File)modelListy.get(i)).getPath());
                            }
                            listaSciezek.removeAll(listaSciezek);
                        }
                    }
               zOutS.close();         
               }
               catch(IOException e)
               {
                   System.out.println(e.getMessage());
               }
   
            }
        }
        private void zipuj(ZipOutputStream zOutS, File sciezkaPliku, byte [] tmpData, String sciezkaBazowa ) throws IOException
        {
             BufferedInputStream inS = new BufferedInputStream(new FileInputStream(sciezkaPliku),BUFFOR);

             zOutS.putNextEntry(new ZipEntry(sciezkaPliku.getPath().substring(sciezkaBazowa.lastIndexOf(File.separator)+1)));
            int counter;
             while ((counter = inS.read(tmpData, 0, BUFFOR))!= -1)
                zOutS.write(tmpData, 0 , counter);

             zOutS.closeEntry();
            inS.close();
        }
        public static final int BUFFOR = 1024;
        
        private void wypiszSciezki (File nazwaSciezki)
        {
            String [] nazwaPlikowIKatologow = nazwaSciezki.list();
            
            for (int i = 0; i < nazwaPlikowIKatologow.length; i++)
            {
                File p = new File(nazwaSciezki.getPath(), nazwaPlikowIKatologow[i]);
                        
                if(p.isFile()) 
                    listaSciezek.add(p);
                if(p.isDirectory())
                    wypiszSciezki(new File (p.getPath()));
            }
            
        }
        ArrayList listaSciezek = new ArrayList();
    }
    
}
