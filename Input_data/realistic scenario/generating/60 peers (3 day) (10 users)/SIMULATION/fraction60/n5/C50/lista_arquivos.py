import glob

########################## LEITURA
lista = glob.glob('*')
somas = []
for nome in lista:
    soma = 0
    if(nome != 'lista_arquivos.py'):
        arquivo = open(nome,'r')
        linhas = arquivo.readlines()
        for x in range(1,len(linhas)):
            valores = linhas[x].split(',')
            soma+= float(valores[1])
        seed = nome.split('-')[1]
        somas.append([seed,soma])
arquivo.close()

############################ ESCRITA
arquivo = open('resultado.csv','a')
for i in somas:
    arquivo.write(i[0] + "#" + str(i[1]) + "\n")
arquivo.close()
print('terminou')
        
                
                
        
